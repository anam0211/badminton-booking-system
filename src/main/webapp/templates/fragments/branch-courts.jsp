<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<h2>${court.name}</h2>

<p><b>Cơ sở:</b> ${court.branch.name}</p>
<p><b>Địa chỉ:</b> ${court.branch.address}</p>

<p>Loại: ${court.type}</p>
<p>Trạng thái: ${court.status}</p>

<h3>Khung giờ</h3>
<ul>
    <c:forEach var="s" items="${slots}">
        <li>${s.slotName} (${s.startTime} - ${s.endTime})</li>
    </c:forEach>
</ul>

<h3>Bảng giá</h3>
<table border="1">
<tr>
    <th>Slot</th>
    <th>Giá</th>
</tr>

<c:forEach var="p" items="${prices}">
<tr>
    <td>${p.timeSlotId}</td>
    <td>${p.price}</td>
</tr>
</c:forEach>

</table>