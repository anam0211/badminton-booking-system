<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ page contentType="text/html;charset=UTF-8" %>

<style>
.container { width: 90%; margin: auto; font-family: Arial; }

.header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 20px;
}

.btn {
    padding: 8px 12px;
    border-radius: 6px;
    text-decoration: none;
    color: white;
}

.btn-create {
    background: #4caf50;
    padding: 5px 10px;
    font-size: 13px;
}
.btn-edit { background: #2196f3; }
.btn-delete { background: #f44336; border: none; }

.table {
    width: 100%;
    border-collapse: collapse;
    table-layout: fixed;
}

.table th, .table td {
    padding: 10px;
    text-align: center;
    vertical-align: middle;
}

.table th {
    background: #e8f5e9;
}

</style>



<div class="container">

<div class="header">
    <h2>Danh sách sân</h2>



    <a class="btn btn-create"
       href="${pageContext.request.contextPath}/admin/courts?action=create&branchId=${branchId}">
        + Tạo sân
    </a>
</div>

<table class="table">
    <tr>
        <th>ID</th>
        <th>Tên</th>
        <th>Loại</th>
        <th>Trạng thái</th>
        <th>Action</th>
    </tr>

    <c:forEach var="c" items="${courts}">
        <tr>
            <td>${c.id}</td>
            <td>${c.name}</td>
            <td>${c.type}</td>
            <td>${c.status}</td>
            <td>
                <div style="display:flex; gap:10px; align-items:center;">

                    <a class="btn btn-edit"
                       href="${pageContext.request.contextPath}/admin/courts?action=edit&id=${c.id}&branchId=${branchId}">
                        Edit
                    </a>

                    <form action="${pageContext.request.contextPath}/admin/courts"
                          method="post"
                          style="margin:0;">
                        <input type="hidden" name="action" value="delete"/>
                        <input type="hidden" name="id" value="${c.id}"/>
                        <input type="hidden" name="branchId" value="${branchId}"/>

                        <button class="btn btn-delete" type="submit">Delete</button>
                    </form>

                </div>
            </td>
        </tr>
    </c:forEach>
</table>

</div>