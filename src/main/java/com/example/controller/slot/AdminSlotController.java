package com.example.controller.slot;

import com.example.model.Slot;
import com.example.service.SlotService;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/admin/slots")
public class AdminSlotController extends HttpServlet {

    private SlotService service = new SlotService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        try {
            String action = req.getParameter("action");

            if ("create".equals(action)) {
                req.getRequestDispatcher("/templates/admin/slot/create.jsp")
                        .forward(req, resp);
                return;
            }

            req.setAttribute("slots", service.getAll());

            req.getRequestDispatcher("/templates/admin/slot/list.jsp")
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

            if ("create".equals(action)) {
                Slot s = new Slot();

                s.setStartTime(req.getParameter("startTime"));
                s.setEndTime(req.getParameter("endTime"));
                s.setSlotName(req.getParameter("slotName"));

                service.create(s);
            }

            else if ("delete".equals(action)) {
                int id = Integer.parseInt(req.getParameter("id"));
                service.delete(id);
            }

            resp.sendRedirect(req.getContextPath() + "/admin/slots");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}