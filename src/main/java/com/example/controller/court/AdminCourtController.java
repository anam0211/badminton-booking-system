package com.example.controller.court;

import com.example.model.*;
import com.example.service.*;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet("/admin/courts")
public class AdminCourtController extends HttpServlet {

    private CourtService service = new CourtService();
    private SlotService slotService = new SlotService();
    private PriceService priceService = new PriceService();

    // ================= GET =================
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        try {
            String action = req.getParameter("action");

            // ========= EDIT =========
            if ("edit".equals(action)) {

                Long id = Long.parseLong(req.getParameter("id"));

                Court c = service.getById(id);

                List<Price> prices = priceService.getByBranch(c.getBranchId());

                req.setAttribute("court", c);
                req.setAttribute("prices", prices);
                req.setAttribute("branchId", c.getBranchId());

                req.getRequestDispatcher("/templates/admin/court/edit.jsp")
                        .forward(req, resp);
                return;
            }

            // ========= CREATE PAGE =========
            if ("create".equals(action)) {

                String branchParam = req.getParameter("branchId");

                if (branchParam == null) {
                    resp.getWriter().println("Missing branchId");
                    return;
                }

                Long branchId = Long.parseLong(branchParam);

                req.setAttribute("branchId", branchId);

                req.getRequestDispatcher("/templates/admin/court/create.jsp")
                        .forward(req, resp);
                return;
            }

            // ========= LIST =========
            String branchParam = req.getParameter("branchId");

            if (branchParam == null) {
                resp.getWriter().println("Missing branchId");
                return;
            }

            Long branchId = Long.parseLong(branchParam);

            req.setAttribute("courts", service.getByBranch(branchId));
            req.setAttribute("branchId", branchId);

            req.getRequestDispatcher("/templates/admin/court/list.jsp")
                    .forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= POST =================
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        try {
            String action = req.getParameter("action");

            // ========= CREATE =========
            if ("create".equals(action)) {

                Long branchId = Long.parseLong(req.getParameter("branchId"));

                Court c = new Court();
                c.setName(req.getParameter("name"));
                c.setType(req.getParameter("type"));
                c.setBranchId(branchId);

                service.createAndReturnId(c);

                // xử lý slot + price
                String[] slots = req.getParameterValues("slotText");
                String[] prices = req.getParameterValues("price");

                if (slots != null && prices != null) {

                    for (int i = 0; i < slots.length; i++) {

                        if (slots[i] == null || slots[i].isEmpty()) continue;

                        String slotText = slots[i];
                        Double priceValue = Double.parseDouble(prices[i]);

                        String[] parts = slotText.split("-");

                        if (parts.length < 2) continue;

                        String start = parts[0].trim().replace("h", "") + ":00";
                        String end = parts[1].trim().replace("h", "") + ":00";

                        Slot s = new Slot();
                        s.setStartTime(start);
                        s.setEndTime(end);
                        s.setSlotName(slotText);

                        int slotId = slotService.createAndReturnId(s);

                        Price p = new Price();
                        p.setBranchId(branchId);
                        p.setTimeSlotId(slotId);
                        p.setPrice(priceValue);

                        priceService.create(p);
                    }
                }

                resp.sendRedirect(req.getContextPath()
                        + "/admin/courts?branchId=" + branchId);
            }

            // ========= UPDATE =========

            // ========= UPDATE =========
            else if ("update".equals(action)) {

                Long id = Long.parseLong(req.getParameter("id"));
                Long branchId = Long.parseLong(req.getParameter("branchId"));

                // ===== 1. UPDATE COURT =====
                Court c = new Court();
                c.setId(id);
                c.setName(req.getParameter("name"));
                c.setType(req.getParameter("type"));
                c.setStatus(req.getParameter("status"));
                service.update(c);

                // ===== 2. LẤY DATA FORM =====
                String[] slotTexts = req.getParameterValues("slotText");
                String[] priceValues = req.getParameterValues("priceValue");
                String[] priceIds = req.getParameterValues("priceId");
                String[] deleteFlags = req.getParameterValues("deleteFlag");

                if (slotTexts != null && priceValues != null) {
                    for (int i = 0; i < slotTexts.length; i++) {

                        String slotText = slotTexts[i];
                        String priceVal = priceValues[i];

                        if (slotText == null || slotText.isEmpty()) continue;
                        if (priceVal == null || priceVal.isEmpty()) continue;

                        Double price = Double.parseDouble(priceVal);

                        boolean isDelete = false;
                        if (deleteFlags != null && i < deleteFlags.length) {
                            isDelete = "true".equals(deleteFlags[i]);
                        }

                        // ===== CASE 1: XOÁ =====
                        if (isDelete) {
                            if (priceIds != null && i < priceIds.length && priceIds[i] != null && !priceIds[i].isEmpty()) {
                                Long priceId = Long.parseLong(priceIds[i]);
                                priceService.delete(priceId);
                            }
                            continue;
                        }

                        // ===== CASE 2: UPDATE GIÁ CŨ =====
                        if (priceIds != null && i < priceIds.length && priceIds[i] != null && !priceIds[i].isEmpty()) {

                            Price p = new Price();
                            p.setId(Long.parseLong(priceIds[i]));
                            p.setPrice(price);
                            priceService.update(p);

                        }

                        // ===== CASE 3: THÊM MỚI =====
                        else {
                            String[] parts = slotText.split("-");
                            if (parts.length < 2) continue;

                            String start = parts[0].trim().replace("h", "") + ":00";
                            String end = parts[1].trim().replace("h", "") + ":00";

                            // tạo slot
                            Slot s = new Slot();
                            s.setStartTime(start);
                            s.setEndTime(end);
                            s.setSlotName(slotText);
                            int slotId = slotService.createAndReturnId(s);

                            // tạo price
                            Price p = new Price();
                            p.setBranchId(branchId);
                            p.setTimeSlotId(slotId);
                            p.setPrice(price);
                            priceService.create(p);
                        }
                    }
                }

                // ===== 3. REDIRECT =====
                resp.sendRedirect(req.getContextPath() + "/admin/courts?branchId=" + branchId);
            }



            // ========= DELETE =========
            else if ("delete".equals(action)) {

                Long id = Long.parseLong(req.getParameter("id"));
                Long branchId = Long.parseLong(req.getParameter("branchId"));

                service.delete(id);

                resp.sendRedirect(req.getContextPath()
                        + "/admin/courts?branchId=" + branchId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}