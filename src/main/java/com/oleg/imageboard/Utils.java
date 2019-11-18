package com.oleg.imageboard;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Utils {

    public static BufferedImage createThumb(BufferedImage in, int w, int h) {
        // scale w, h to keep aspect constant
        double outputAspect = 1.0*w/h;
        double inputAspect = 1.0*in.getWidth()/in.getHeight();
        if (outputAspect < inputAspect) {
            // width is limiting factor; adjust height to keep aspect
            h = (int)(w/inputAspect);
        } else {
            // height is limiting factor; adjust width to keep aspect
            w = (int)(h*inputAspect);
        }
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = bi.createGraphics();
        g2.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(in, 0, 0, w, h, null);
        g2.dispose();
        return bi;
    }
}
