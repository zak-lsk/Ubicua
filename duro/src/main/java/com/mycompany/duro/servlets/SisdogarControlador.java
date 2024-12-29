package com.mycompany.duro.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SisdogarControlador extends HttpServlet{
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String parametro = request.getParameter("parametro");
        System.out.println("Recibido: " + parametro);
        response.getWriter().write("Acción recibida: " + parametro);
    }
}
