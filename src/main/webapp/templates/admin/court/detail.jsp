<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<h2>Admin - ${court.name}</h2>

<form action="admin-court" method="post">
    <input type="hidden" name="id" value="${court.id}"/>

    Tên: <input name="name" value="${court.name}"/><br/>
    Loại: <input name="type" value="${court.type}"/><br/>

    <button name="action" value="update">Cập nhật</button>
    <button name="action" value="delete">Xóa</button>
</form>