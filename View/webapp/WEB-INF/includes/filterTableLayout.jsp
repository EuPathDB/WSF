<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="wdk" tagdir="/WEB-INF/tags/wdk" %>

<c:set var="answerValue" value="${requestScope.answer_value}"/>
<c:set var="strategyId" value="${requestScope.strategy_id}"/>
<c:set var="stepId" value="${requestScope.step_id}"/>

<c:set var="layout" value="${requestScope.filter_layout}"/>

<table border="1" cellspacing="1">
  <c:choose>
    <c:when test="layout.vertical"> <%-- vertically aligned table --%>
      <c:forEach items="${layout.instances}" var="instance">
        <tr>
          <th>${instance.displayName}</th>
          <td>
            <wdk:filterInstance strategyId="${strategyId}" stepId="${stepId}" answerValue="${answerValue}" instanceName="${instance.name}" />
          </td>
        </tr>
      </c:forEach>
    </c:when>
    <c:otherwise> <%-- horizontally aligned table --%>
      <tr>
        <c:forEach items="${layout.instances}" var="instance">
          <th>${instance.displayName}</th>
        </c:forEach>
      </tr>
      <tr>
        <c:forEach items="${layout.instances}" var="instance">
          <td>
            <wdk:filterInstance strategyId="${strategyId}" stepId="${stepId}" answerValue="${answerValue}" instanceName="${instance.name}" />
          </td>
        </c:forEach>
      </tr>
    </c:otherwise>
  </c:choose>
</table>
