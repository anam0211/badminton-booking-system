<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

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
    font-size: 24px;
    font-weight: bold;
    color: #2e7d32;
}

.badge {
    display: inline-block;
    padding: 4px 10px;
    background: #e0f2f1;
    border-radius: 20px;
    color: #00796b;
    margin-top: 5px;
}

.section {
    margin-top: 30px;
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

.price {
    color: #2e7d32;
    font-weight: bold;
}
</style>



<div class="container">

    <div class="card">
        <div class="title">${court.name}</div>
        <div class="badge">${court.type}</div>

        <p>
            <c:if test="${court.branch != null}">
                ${court.branch.address}
            </c:if>
        </p>

        <p>Trạng thái: <b>${court.status}</b></p>
    </div>


    <!-- PRICE -->
    <div class="section">
        <h3>Bảng giá</h3>

        <table class="table">
            <thead>
                <tr>
                    <th>Khung giờ</th>
                    <th>Giá</th>
                </tr>
            </thead>

            <tbody>
                <c:forEach var="p" items="${prices}">
                    <tr>
                        <td><c:out value="${p.slotName}" /></td>
                        <td class="price">
                            <c:out value="${p.price}" /> VND
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>

</div>

