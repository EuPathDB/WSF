package org.gusdb.wdk.controller;

import java.io.IOException;
import java.security.MessageDigest;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.UserFactoryBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

public class CheckLoginFilter implements Filter {
    private FilterConfig config = null;
    private ServletContext context = null;

    public void init(FilterConfig filterConfig) throws ServletException {
        this.config = filterConfig;
        this.context = config.getServletContext();
        this.context.log("Filter CheckLoginFilter initialized.");
    }

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // load model, user
        WdkModelBean wdkModel = (WdkModelBean) context.getAttribute(CConstants.WDK_MODEL_KEY);
        UserBean wdkUser = (UserBean) req.getSession().getAttribute(
                CConstants.WDK_USER_KEY);
        if (wdkUser == null) {
            try {
                wdkUser = wdkModel.getUserFactory().getGuestUser();
                req.getSession().setAttribute(CConstants.WDK_USER_KEY, wdkUser);
            } catch (Exception ex) {
                // I have no idea what to do here...
                System.err.println("Caught WdkUserException in CheckLoginFilter: "
                        + ex);
            }
        }

        Cookie cookies[] = req.getCookies();
        Cookie loginCookie = null;

        if (cookies != null) {
            for (int i = 0; i < cookies.length; ++i) {
                if (cookies[i].getName().equals(CConstants.WDK_LOGIN_COOKIE_KEY)) {
                    loginCookie = cookies[i];
                    break;
                }
            }
        }

        if (loginCookie != null) {
            if (wdkUser == null
                    || !loginCookie.getValue().contains(wdkUser.getEmail())) {
                try {
                    UserFactoryBean factory = wdkModel.getUserFactory();

                    // Check if cookie has been modified since it was set by the
                    // application.
                    MessageDigest digest = MessageDigest.getInstance("MD5");
                    String secretValue = wdkModel.getSecretKey();

                    secretValue = loginCookie.getValue().substring(0,
                            loginCookie.getValue().lastIndexOf("-"))
                            + secretValue;
                    String cookieHash = loginCookie.getValue().substring(
                            loginCookie.getValue().lastIndexOf("-") + 1);

                    byte[] encrypted = digest.digest(secretValue.getBytes());
                    // convert each byte into hex format
                    StringBuffer buffer = new StringBuffer();
                    for (byte code : encrypted) {
                        buffer.append(Integer.toHexString(code & 0xFF));
                    }

                    secretValue = buffer.toString();

                    if (!secretValue.equals(cookieHash)) {
                        System.out.println("Secret Value: " + secretValue);
                        System.out.println("Cookie Hash: " + cookieHash);
                        throw new Exception(
                                "Login cookie is invalid and must be deleted.");
                    }

                    String signature;
                    String[] cookieParts = loginCookie.getValue().split("-");

                    signature = cookieParts[0] + "_" + cookieParts[1];

                    encrypted = digest.digest(signature.getBytes());
                    // convert each byte into hex format
                    buffer = new StringBuffer();
                    for (byte code : encrypted) {
                        buffer.append(Integer.toHexString(code & 0xFF));
                    }

                    signature = buffer.toString();

                    UserBean user = factory.getUser(signature);

                    if (loginCookie.getValue().contains("remember")) {
                        loginCookie.setMaxAge(java.lang.Integer.MAX_VALUE / 256);
                        loginCookie.setPath("/");
                        res.addCookie(loginCookie);
                    }

                    req.getSession().setAttribute(CConstants.WDK_USER_KEY, user);
                    req.getSession().setAttribute(
                            CConstants.WDK_LOGIN_ERROR_KEY, "");
                } catch (Exception ex) {
                    System.out.println("Caught exception while checking login "
                            + "cookie: " + ex);
                    // tell browser to delete cookie if we had a problem
                    loginCookie.setMaxAge(0);
                    loginCookie.setPath("/");
                    res.addCookie(loginCookie);
                }
            }
        }

        chain.doFilter(request, response);
    }

    public void destroy() {
        this.context = null;
        this.config = null;
    }
}