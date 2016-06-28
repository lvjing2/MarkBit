/**
 * Paintroid: An image manipulation application for Android.
 * Copyright (C) 2010-2015 The Catrobat Team
 * (<http://developer.catrobat.org/credits>)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.liwn.zzl.markbit.tools;

//import com.liwn.zzl.markbit.tools.implementation.ResizeTool;

import android.app.Activity;

import com.liwn.zzl.markbit.tools.implementation.CursorTool;
import com.liwn.zzl.markbit.tools.implementation.DrawTool;
import com.liwn.zzl.markbit.tools.implementation.EraserTool;
import com.liwn.zzl.markbit.tools.implementation.FillTool;
import com.liwn.zzl.markbit.tools.implementation.FlipTool;
import com.liwn.zzl.markbit.tools.implementation.GeometricFillTool;
import com.liwn.zzl.markbit.tools.implementation.ImportTool;
import com.liwn.zzl.markbit.tools.implementation.LineTool;
import com.liwn.zzl.markbit.tools.implementation.MoveZoomTool;
import com.liwn.zzl.markbit.tools.implementation.PipetteTool;
import com.liwn.zzl.markbit.tools.implementation.RotationTool;
import com.liwn.zzl.markbit.tools.implementation.StampTool;
import com.liwn.zzl.markbit.tools.implementation.TextTool;

public class ToolFactory {

    public static Tool createTool(Activity context, ToolType toolType) {
        switch (toolType) {
            case BRUSH:
                return new DrawTool(context, toolType);
            case TEXT:
                return new TextTool(context, toolType);
            case CURSOR:
                return new CursorTool(context, toolType);
            case ELLIPSE:
                return new GeometricFillTool(context, toolType);
            case STAMP:
                return new StampTool(context, toolType);
            case IMPORTPNG:
                return new ImportTool(context, toolType);
            case PIPETTE:
                return new PipetteTool(context, toolType);
            case FILL:
                return new FillTool(context, toolType);
//		case RESIZE:
//			return new ResizeTool(context, toolType);
            case RECT:
                return new GeometricFillTool(context, toolType);
            case ERASER:
                return new EraserTool(context, toolType);
            case FLIP:
                return new FlipTool(context, toolType);
            case LINE:
                return new LineTool(context, toolType);
            case MOVE:
            case ZOOM:
                return new MoveZoomTool(context, toolType);
            case ROTATE:
                return new RotationTool(context, toolType);
            default:
                break;
        }
        return new DrawTool(context, ToolType.BRUSH);

    }

}
