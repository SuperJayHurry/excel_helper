<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="管理员面板"/>
<%@ include file="/WEB-INF/jsp/fragments/header.jsp" %>

<section class="card">
    <h2>发布新的汇总任务</h2>
    <form method="post" action="/admin/templates" enctype="multipart/form-data" class="grid-form">
        <div>
            <label>任务名称
                <input type="text" name="name" value="${templateForm.name}" required>
            </label>
        </div>
        <div>
            <label>截止日期
                <input type="date" name="deadline" value="${templateForm.deadline}" required>
            </label>
        </div>
        <div class="full-row">
            <label>任务描述
                <textarea name="description" rows="2">${templateForm.description}</textarea>
            </label>
        </div>
        <div>
            <label>发送范围
                <select name="targetScope">
                    <option value="ALL">全院教师</option>
                    <c:forEach items="${departments}" var="dept">
                        <option value="${dept.name()}">${dept.displayName}</option>
                    </c:forEach>
                    <option value="CUSTOM">自定义</option>
                </select>
            </label>
        </div>
        <div>
            <label>模板 Excel
                <input type="file" name="templateFile" accept=".xls,.xlsx" required>
            </label>
            <c:if test="${not empty fileError}">
                <span class="error">${fileError}</span>
            </c:if>
        </div>
        <div class="full-row">
            <label>指定教师（自定义时生效）
                <select name="teacherIds" multiple size="4">
                    <c:forEach items="${teachers}" var="teacher">
                        <option value="${teacher.id}">${teacher.fullName}（${teacher.department.displayName}）</option>
                    </c:forEach>
                </select>
            </label>
        </div>
        <div class="full-row">
            <button type="submit">发布任务</button>
        </div>
    </form>
</section>

<section class="card">
    <h2>已发布任务</h2>
    <table>
        <thead>
        <tr>
            <th>名称</th>
            <th>截止日期</th>
            <th>模板</th>
            <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${tasks}" var="task">
            <tr>
                <td>${task.name}</td>
                <td><c:out value="${task.deadline}"/></td>
                <td><a href="/files?path=${task.templateFilePath}">下载</a></td>
                <td>
                    <a class="btn" href="/admin/templates/${task.id}">查看进度</a>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</section>

</main>
</body>
</html>

