package com.example.controller.court;

import com.example.model.Court;
import com.example.service.*;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/court-detail")
public class CourtController extends HttpServlet {

    private CourtService courtService = new CourtService();
    private SlotService slotService = new SlotService();
    private PriceService priceService = new PriceService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        try {
            Long courtId = Long.parseLong(req.getParameter("id"));
            Court court = courtService.getById(courtId);

            req.setAttribute("court", court);
            req.setAttribute("slots", slotService.getAll());
            req.setAttribute("prices",
                    priceService.getByBranch(court.getBranchId()));

            req.getRequestDispatcher("/templates/client/court-detail.jsp")
                    .forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}