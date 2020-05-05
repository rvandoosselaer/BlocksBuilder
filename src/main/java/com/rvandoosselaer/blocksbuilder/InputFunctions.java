package com.rvandoosselaer.blocksbuilder;

import com.jme3.input.KeyInput;
import com.simsilica.lemur.input.Axis;
import com.simsilica.lemur.input.Button;
import com.simsilica.lemur.input.FunctionId;
import com.simsilica.lemur.input.InputMapper;
import com.simsilica.lemur.input.InputState;
import lombok.extern.slf4j.Slf4j;

/**
 * Defines the input functions and their default mappings.
 *
 * @author: rvandoosselaer
 */
@Slf4j
public class InputFunctions {

    // groups
    public static final String CAMERA_INPUT_GROUP = "camera.input";

    // camera input
    public static final FunctionId F_X_ROTATE = new FunctionId(CAMERA_INPUT_GROUP, "x-rotate");
    public static final FunctionId F_Y_ROTATE = new FunctionId(CAMERA_INPUT_GROUP, "y-rotate");
    public static final FunctionId F_ZOOM = new FunctionId(CAMERA_INPUT_GROUP, "zoom");
    public static final FunctionId F_DRAG = new FunctionId(CAMERA_INPUT_GROUP, "drag");
    public static final FunctionId F_MOVE = new FunctionId(CAMERA_INPUT_GROUP, "move");
    public static final FunctionId F_STRAFE = new FunctionId(CAMERA_INPUT_GROUP, "strafe");
    public static final FunctionId F_CENTER = new FunctionId(CAMERA_INPUT_GROUP, "center");

    public static void initializeDefaultMappings(InputMapper inputMapper) {
        // pitch movement (nose up / down) is done with the mouse Y-axis
        // yaw movement (nose left / right) is done with the mouse X-axis
        inputMapper.map(F_X_ROTATE, Axis.MOUSE_Y);
        inputMapper.map(F_Y_ROTATE, Axis.MOUSE_X);
        inputMapper.map(F_ZOOM, Axis.MOUSE_WHEEL);
        inputMapper.map(F_ZOOM, InputState.Positive, KeyInput.KEY_PGUP);
        inputMapper.map(F_ZOOM, InputState.Negative, KeyInput.KEY_PGDN);
        inputMapper.map(F_DRAG, Button.MOUSE_BUTTON3);
        inputMapper.map(F_DRAG, KeyInput.KEY_V);
        inputMapper.map(F_MOVE, KeyInput.KEY_W);
        inputMapper.map(F_MOVE, KeyInput.KEY_UP);
        inputMapper.map(F_MOVE, InputState.Negative, KeyInput.KEY_S);
        inputMapper.map(F_MOVE, InputState.Negative, KeyInput.KEY_DOWN);
        inputMapper.map(F_STRAFE, InputState.Negative, KeyInput.KEY_D);
        inputMapper.map(F_STRAFE, InputState.Negative, KeyInput.KEY_RIGHT);
        inputMapper.map(F_STRAFE, KeyInput.KEY_A);
        inputMapper.map(F_STRAFE, KeyInput.KEY_LEFT);
        inputMapper.map(F_CENTER, KeyInput.KEY_HOME);

        log.trace("Initialized input mappings.");
    }

    public static void removeDefaultMappings(InputMapper inputMapper) {
        inputMapper.getMappings(F_X_ROTATE).forEach(inputMapper::removeMapping);
        inputMapper.getMappings(F_Y_ROTATE).forEach(inputMapper::removeMapping);
        inputMapper.getMappings(F_ZOOM).forEach(inputMapper::removeMapping);
        inputMapper.getMappings(F_DRAG).forEach(inputMapper::removeMapping);
        inputMapper.getMappings(F_MOVE).forEach(inputMapper::removeMapping);
        inputMapper.getMappings(F_STRAFE).forEach(inputMapper::removeMapping);
        inputMapper.getMappings(F_CENTER).forEach(inputMapper::removeMapping);

        log.trace("Removed input mappings.");
    }

}
