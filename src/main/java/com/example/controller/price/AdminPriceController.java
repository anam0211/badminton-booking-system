package com.example.controller.price;

import com.example.model.Price;
import com.example.service.PriceService;
import com.example.service.SlotService;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/admin/prices")
public class AdminPriceController extends HttpServlet {

    private PriceService service = new PriceService();
    private SlotService slotService = new SlotService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        try {
            String action = req.getParameter("action");

            String branchParam = req.getParameter("branchId");
            Long branchId = (branchParam != null) ? Long.parseLong(branchParam) : null;

            if ("create".equals(action)) {
                req.setAttribute("slots", slotService.getAll());
                req.setAttribute("branchId", branchId);

                req.getRequestDispatcher("/templates/admin/price/create.jsp")
                        .forward(req, resp);
                return;
            }

            if (branchId != null) {
                req.setAttribute("prices", service.getByBranch(branchId));
            }

            req.setAttribute("branchId", branchId);

            req.getRequestDispatcher("/templates/admin/price/list.jsp")
                    .forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        try {
            String action = req.getParameter("action");

            String branchId = req.getParameter("branchId");
            String redirectFrom = req.getParameter("redirect"); // 🔥 QUAN TRỌNG

            if ("create".equals(action)) {
                Price p = new Price();
                p.setBranchId(Long.parseLong(branchId));
                p.setTimeSlotId(Integer.parseInt(req.getParameter("timeSlotId")));
                p.setPrice(Double.parseDouble(req.getParameter("price")));

                service.create(p);
            }

            else if ("update".equals(action)) {
                Price p = new Price();
                p.setId(Long.parseLong(req.getParameter("id")));
                p.setPrice(Double.parseDouble(req.getParameter("price")));

                service.update(p);
            }

            else if ("delete".equals(action)) {
                Long id = Long.parseLong(req.getParameter("id"));
                service.delete(id);
            }


            if ("court".equals(redirectFrom)) {
                String courtId = req.getParameter("courtId");
                resp.sendRedirect(req.getContextPath()
                        + "/admin/courts?action=edit&id=" + courtId);
            } else {
                resp.sendRedirect(req.getContextPath()
                        + "/admin/prices?branchId=" + branchId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}