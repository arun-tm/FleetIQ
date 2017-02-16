package com.teramatrix.fleetiq.velocimeter.velocimeterlibrary.painter.progress;


import com.teramatrix.fleetiq.velocimeter.velocimeterlibrary.painter.Painter;

/**
 * @author Adrián García Lomas
 */
public interface ProgressVelocimeterPainter extends Painter {

  public void setValue(float value);
}
