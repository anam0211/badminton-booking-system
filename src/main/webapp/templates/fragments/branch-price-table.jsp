<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<table>
    <thead>
        <tr>
            <th>Khung giờ</th>
            <th>Loại sân</th>
            <th>Giá</th>
        </tr>
    </thead>

    <tbody>
        <tr th:each="p : ${prices}">
            <td th:text="${p.timeSlotId}"></td>
            <td th:text="${p.courtType}"></td>
            <td th:text="${p.price}"></td>
        </tr>
    </tbody>
</table>