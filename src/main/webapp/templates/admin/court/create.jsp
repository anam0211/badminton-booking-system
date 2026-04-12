<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<style>
.container {
    width: 90%;
    margin: auto;
    font-family: Arial;
}

.card {
    background: #f5f7f6;
    padding: 20px;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.title {
    font-size: 22px;
    font-weight: bold;
    color: #2e7d32;
    margin-bottom: 15px;
}

.form-group {
    margin-bottom: 15px;
}

input, select {
    padding: 8px;
    width: 100%;
    border-radius: 6px;
    border: 1px solid #ccc;
}

.table {
    width: 100%;
    border-collapse: collapse;
    margin-top: 10px;
}

.table th {
    background: #e8f5e9;
    padding: 10px;
}

.table td {
    padding: 10px;
    border-bottom: 1px solid #ddd;
}

.btn {
    padding: 6px 10px;
    border-radius: 6px;
    border: none;
    cursor: pointer;
}

.btn-add {
    background: #4caf50;
    color: white;
}

.btn-delete {
    background: #f44336;
    color: white;
}

.btn-submit {
    background: #2e7d32;
    color: white;
    padding: 10px 15px;
}

.row-flex {
    display: flex;
    gap: 10px;
    align-items: center;
}
</style>

<div class="container">
    <div class="card">
        <div class="title">Tạo sân mới</div>

        <form action="${pageContext.request.contextPath}/admin/courts" method="post">
            <input type="hidden" name="action" value="create"/>
            <input type="hidden" name="branchId" value="${branchId}"/>

            <!-- INFO -->
            <div class="form-group">
                <label>Tên sân</label>
                <input type="text" name="name" required/>
            </div>

            <div class="form-group">
                <label>Loại sân</label>
                <input type="text" name="type" required/>
            </div>

            <hr/>

            <!-- SLOT + PRICE -->
            <h3>Khung giờ - Giá</h3>

            <table class="table" id="priceTable">
                <tr>
                    <th>Khung giờ</th>
                    <th>Giá</th>
                    <th></th>
                </tr>

                <!-- dòng mặc định -->
                <tr>
                    <td>
                        <input type="text" name="slotText" placeholder="VD: 10h - 12h"/>
                    </td>
                    <td>
                        <input type="number" name="price"/>
                    </td>
                    <td>
                        <button type="button" class="btn btn-delete" onclick="removeRow(this)">X</button>
                    </td>
                </tr>
            </table>

            <br/>
            <button type="button" class="btn btn-add" onclick="addRow()">+ Thêm dòng</button>

            <br/><br/>
            <button class="btn btn-submit">Tạo sân</button>
        </form>
    </div>
</div>

<script>
function addRow() {
    let table = document.getElementById("priceTable");
    let row = table.insertRow();

    row.innerHTML = `
        <td>
            <input type="text" name="slotText" placeholder="VD: 10h - 12h"/>
        </td>
        <td>
            <input type="number" name="price"/>
        </td>
        <td>
            <button type="button" class="btn btn-delete" onclick="removeRow(this)">X</button>
        </td>
    `;
}

function removeRow(btn) {
    let row = btn.parentNode.parentNode;
    row.remove();
}
</script>