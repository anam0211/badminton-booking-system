<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<style>
.container { width: 90%; margin: auto; font-family: Arial; }

.card {
    background: #f5f7f6;
    padding: 20px;
    border-radius: 12px;
    margin-bottom: 20px;
}

input, select {
    padding: 8px;
    width: 100%;
    margin: 5px 0 10px;
}

.btn {
    padding: 8px 12px;
    border: none;
    border-radius: 6px;
    cursor: pointer;
}

.btn-add { background: #4caf50; color: white; }
.btn-delete { background: #f44336; color: white; }
.btn-submit { background: #2196f3; color: white; }

.table {
    width: 100%;
    border-collapse: collapse;
}

.table th {
    background: #e8f5e9;
}

.table td, .table th {
    padding: 10px;
}
</style>

<div class="container">

<form action="${pageContext.request.contextPath}/admin/courts" method="post">
<input type="hidden" name="action" value="update"/>
<input type="hidden" name="id" value="${court.id}"/>
<input type="hidden" name="branchId" value="${branchId}"/>

<div class="card">
    <h3>Thông tin sân</h3>

    Tên:
    <input type="text" name="name" value="${court.name}"/>

    Loại:
    <input type="text" name="type" value="${court.type}"/>

    Trạng thái:
    <select name="status">
        <option value="AVAILABLE" ${court.status=='AVAILABLE'?'selected':''}>AVAILABLE</option>
        <option value="MAINTENANCE" ${court.status=='MAINTENANCE'?'selected':''}>MAINTENANCE</option>
    </select>
</div>

<div class="card">
    <h3>Khung giờ - Giá</h3>

    <table class="table" id="priceTable">
        <tr>
            <th>Khung giờ</th>
            <th>Giá</th>
            <th></th>
        </tr>

        <c:forEach var="p" items="${prices}">
            <tr>
                <td>
                    <input type="text" name="slotText" value="${p.slotName}"/>
                </td>

                <td>
                    <input type="hidden" name="priceId" value="${p.id}"/>
                    <input type="hidden" name="deleteFlag" value="false"/>
                    <input type="number" name="priceValue" value="${p.price}"/>
                </td>

                <td>
                    <button type="button" onclick="removeRow(this)">X</button>
                </td>
            </tr>
        </c:forEach>

    </table>

    <br/>
    <button type="button" class="btn btn-add" onclick="addRow()">+ Thêm</button>
</div>

<button class="btn btn-submit" type="submit">Cập nhật</button>

</form>
</div>

<script>
function addRow() {
    let table = document.getElementById("priceTable");
    let row = table.insertRow();

    row.innerHTML = `
        <tr>
            <td>
                <input type="text" name="slotText" value="${p.slotName}"/>
            </td>

            <td>
                <input type="hidden" name="priceId" value="${p.id}"/>
                <input type="hidden" name="deleteFlag" value="false"/>
                <input type="number" name="priceValue" value="${p.price}"/>
            </td>

            <td>
                <button type="button" onclick="removeRow(this)">X</button>
            </td>
        </tr>
    `;
}

function removeRow(btn) {
    let row = btn.closest("tr");

    // đánh dấu xoá
    let deleteInput = row.querySelector("input[name='deleteFlag']");
    if (deleteInput) {
        deleteInput.value = "true";
        row.style.display = "none";
    } else {
        row.remove();
    }
}
</script>