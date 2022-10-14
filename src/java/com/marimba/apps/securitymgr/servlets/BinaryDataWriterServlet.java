package com.marimba.apps.securitymgr.servlets;

import org.apache.commons.codec.binary.Base64;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: intteiva
 * Date: Apr 26, 2017
 * Time: 4:33:33 PM
 * To change this template use File | Settings | File Templates.
 */
// Refer: http://stackoverflow.com/questions/27957632/how-to-store-canvas-image-to-server-using-struts2-mvc

public class BinaryDataWriterServlet extends HttpServlet {
    static final String SAVE_DIR = "C:\\Test\\Upload\\";
    static final int BUFFER_SIZE = 4096;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String fileName = request.getParameter("docType");
            String img64 = request.getParameter("data");
            byte[] valueDecoded = Base64.decodeBase64(img64.getBytes());
            File saveFile = new File(SAVE_DIR + fileName + ".png");
            saveFile.createNewFile();
            FileOutputStream file1 = new FileOutputStream(saveFile);
            file1.write(valueDecoded);
            file1.close();

            System.out.println("File written to: " + saveFile.getAbsolutePath());

            response.getWriter().print("UPLOAD DONE");
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}
