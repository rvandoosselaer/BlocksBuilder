package com.rvandoosselaer.blocksbuilder;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.input.AnalogFunctionListener;
import com.simsilica.lemur.input.FunctionId;
import com.simsilica.lemur.input.InputMapper;
import com.simsilica.lemur.input.InputState;
import com.simsilica.lemur.input.StateFunctionListener;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import static com.rvandoosselaer.blocksbuilder.InputFunctions.CAMERA_INPUT_GROUP;
import static com.rvandoosselaer.blocksbuilder.InputFunctions.F_BOUNCE;
import static com.rvandoosselaer.blocksbuilder.InputFunctions.F_CENTER;
import static com.rvandoosselaer.blocksbuilder.InputFunctions.F_DRAG;
import static com.rvandoosselaer.blocksbuilder.InputFunctions.F_MOVE;
import static com.rvandoosselaer.blocksbuilder.InputFunctions.F_STRAFE;
import static com.rvandoosselaer.blocksbuilder.InputFunctions.F_X_ROTATE;
import static com.rvandoosselaer.blocksbuilder.InputFunctions.F_Y_ROTATE;
import static com.rvandoosselaer.blocksbuilder.InputFunctions.F_ZOOM;

/**
 * An AppState for controlling the camera.
 *
 * @author: rvandoosselaer
 */
@Slf4j
public class CameraState extends BaseAppState implements AnalogFunctionListener, StateFunctionListener {

    /**
     * Actual distance between the camera and the target; the distance will change when smooth motion is enabled
     * and the camera is moving around, but will always return to the targetDistance. This is an internal parameter!
     */
    private float distance = 40f;
    /**
     * distance between the camera and the target
     */
    private float targetDistance = distance;
    /**
     * max zoom-in distance
     */
    private float minDistance = 1f;
    /**
     * max zoom-out distance
     */
    private float maxDistance = 100f;
    /**
     * horizontal and vertical rotation speed
     */
    private float rotationSpeed = 2.5f;
    /**
     * the maximum rotation speed when sudden mouse movement occurs
     */
    private float maximumRotationSpeed = 4f;
    /**
     * zoom speed
     */
    private float zoomSpeed = 50f;
    /**
     * rotation on the Y axis
     */
    private float yaw = FastMath.HALF_PI;
    /**
     * rotation on the X axis
     */
    private float pitch = 30 * FastMath.DEG_TO_RAD;
    /**
     * minimum pitch
     */
    private float minPitch = -89 * FastMath.DEG_TO_RAD;
    /**
     * maximum pitch
     */
    private float maxPitch = 89 * FastMath.DEG_TO_RAD;
    /**
     * camera location
     */
    private final Vector3f cameraPosition = new Vector3f();
    /**
     * original camera focus point. The camera will move to this point when the F_CENTER function is triggered.
     */
    @Getter
    @Setter
    private Vector3f startingTargetLocation = new Vector3f();
    /**
     * current camera focus point
     */
    @Getter
    private Vector3f targetLocation = new Vector3f();
    /**
     * camera focus point offset
     */
    private Vector3f targetLocationOffset = new Vector3f(0, 0, 0);
    /**
     * invert mouse x-axis movement (camera yaw)
     */
    private boolean invertX = false;
    /**
     * invert mouse y-axis movement (camera pitch)
     */
    private boolean invertY = false;
    /**
     * speed when manually moving the camera
     */
    private float moveSpeed = 6f;
    /**
     * the threshold when camera movement is disabled. This is to counter camera jitter
     */
    private float distanceDiscardThreshold = 0.002f;
    /**
     * flag to enable smooth camera movement.
     */
    private boolean smoothMotion = true;
    /**
     * smooth motion sensitivity. the lower the value, the more the 'rubber band' will stretch
     */
    private float smoothMotionSensitivity = 30f;
    /**
     * the range of the rubber band effect of the camera. The camera distance will vary between
     * [(targetDistance + smoothMotionStretchValues.x) - (targetDistance + smoothMotionSensitivity.y)]
     */
    private Vector2f smoothMotionStretchValues = new Vector2f(-1, 1);

    private Camera camera;
    private boolean chasing; // internal flag indicating if the camera should chase/catch up with the target when smooth motion is enabled
    private boolean dragging; // internal flag indicating if the camera is being drag-rotated
    private Vector3f upVector;
    private InputMapper inputMapper;
    private float distanceLerpFactor = 0;
    private Vector3f previousLocation = new Vector3f();

    @Override
    protected void initialize(Application app) {
        camera = app.getCamera();
        upVector = camera.getUp(new Vector3f());
        targetLocation.set(startingTargetLocation);

        inputMapper = GuiGlobals.getInstance().getInputMapper();
        inputMapper.addAnalogListener(this, F_X_ROTATE, F_Y_ROTATE, F_ZOOM, F_MOVE, F_STRAFE, F_BOUNCE);
        inputMapper.addStateListener(this, F_DRAG, F_CENTER);
    }

    @Override
    protected void cleanup(Application app) {
        inputMapper.removeAnalogListener(this, F_X_ROTATE, F_Y_ROTATE, F_ZOOM, F_MOVE, F_STRAFE, F_BOUNCE);
        inputMapper.removeStateListener(this, F_DRAG, F_CENTER);

        // reset the camera position
        camera.setLocation(new Vector3f(0, 0, 10));
        camera.lookAt(Vector3f.ZERO, upVector);
    }

    @Override
    protected void onEnable() {
        inputMapper.activateGroup(CAMERA_INPUT_GROUP);
    }

    @Override
    protected void onDisable() {
        inputMapper.deactivateGroup(CAMERA_INPUT_GROUP);
    }

    @Override
    public void valueActive(FunctionId func, double value, double tpf) {
        if (func == F_X_ROTATE && dragging) {
            calculatePitch(value, tpf);
        } else if (func == F_Y_ROTATE && dragging) {
            calculateYaw(value, tpf);
        } else if (func == F_ZOOM) {
            calculateZoom(value, tpf);
            chasing = true;
        } else if (func == F_MOVE) {
            calculateMove(value, tpf);
            chasing = true;
        } else if (func == F_STRAFE) {
            calculateStrafe(value, tpf);
            chasing = true;
        } else if (func == F_BOUNCE) {
            calculateBounce(value, tpf);
            chasing = true;
        }
    }

    @Override
    public void valueChanged(FunctionId func, InputState value, double tpf) {
        if (func == F_DRAG) {
            // update the dragging boolean and set the cursor accordingly
            dragging = value != InputState.Off;
            GuiGlobals.getInstance().setCursorEventsEnabled(!dragging);
        } else if (func == F_CENTER && value != InputState.Off) {
            targetLocation.set(startingTargetLocation);
        }
    }

    @Override
    public void update(float tpf) {
        Vector3f target = new Vector3f(targetLocation);
        target.addLocal(targetLocationOffset);

        if (smoothMotion) {
            float distanceDifference = target.subtract(previousLocation).length();
            if (distanceDifference > distanceDiscardThreshold) {
                chasing = true;
            }

            if (chasing) {
                distance = target.subtract(cameraPosition).length();
                // clamp the distance
                distance = FastMath.clamp(distance, targetDistance + smoothMotionStretchValues.x, targetDistance + smoothMotionStretchValues.y);
                distanceLerpFactor = Math.min(distanceLerpFactor + (tpf * tpf * smoothMotionSensitivity * 0.05f), 1);
                distance = FastMath.interpolateLinear(distanceLerpFactor, distance, targetDistance);
                if (targetDistance + 0.01f >= distance && targetDistance - 0.01f <= distance) {
                    distanceLerpFactor = 0;
                    chasing = false;
                }
            }
        } else {
            distance = targetDistance;
        }

        float hDistance = (distance) * FastMath.sin(FastMath.HALF_PI - pitch);
        cameraPosition.set(hDistance * FastMath.cos(yaw), (distance) * FastMath.sin(pitch), hDistance * FastMath.sin(yaw));

        // add the target position
        cameraPosition.addLocal(target);

        // set camera location and facing
        camera.setLocation(cameraPosition);
        camera.lookAt(target, upVector);

        previousLocation = target;
    }

    /**
     * calculate and set yaw value (rotation of the y-axis)
     * when translated to an aircraft, a positive value turns the nose to the right, a negative value turns the nose to
     * the left.
     */
    private void calculateYaw(double value, double tpf) {
        // for yaw movement we don't clamp the value
        yaw += value * tpf * rotationSpeed * (invertX ? -1 : 1);
        if (yaw < 0) {
            yaw += FastMath.TWO_PI;
        }
        if (yaw > FastMath.TWO_PI) {
            yaw -= FastMath.TWO_PI;
        }
    }

    /**
     * calculate and set pitch value (rotation of the x-axis)
     * when translated to an aircraft, a positive pitch value will raise the nose up, a negative value will lower the
     * nose down.
     */
    private void calculatePitch(double value, double tpf) {
        // try to scale the input value, when you make a sudden mouse movement, the value can become very high
        double normalizedValue = value > 0 ? Math.min(value, maximumRotationSpeed) : Math.max(value, -maximumRotationSpeed);
        pitch += normalizedValue * tpf * rotationSpeed * (invertY ? -1 : 1);
        pitch = FastMath.clamp(pitch, minPitch, maxPitch);
    }

    /**
     * calculate and set zoom value
     */
    private void calculateZoom(double value, double tpf) {
        targetDistance += -value * tpf * zoomSpeed;
        targetDistance = FastMath.clamp(targetDistance, minDistance, maxDistance);
    }

    /**
     * calculate and set the forward/backward movement value
     */
    private void calculateMove(double value, double tpf) {
        // only move in the x-z pane
        Vector3f movement = camera.getDirection().mult(new Vector3f(1, 0, 1)).normalizeLocal();
        movement.multLocal((float) (value * moveSpeed * tpf));
        targetLocation.addLocal(movement);
    }

    /**
     * calculate and set strafe movement value
     */
    private void calculateStrafe(double value, double tpf) {
        Vector3f strafe = camera.getLeft().mult((float) (value * moveSpeed * tpf));
        targetLocation.addLocal(strafe);
    }

    /**
     * Calculate the up/down movement
     */
    private void calculateBounce(double value, double tpf) {
        Vector3f upDir = Vector3f.UNIT_Y.mult((float) (value * moveSpeed * tpf));
        targetLocation.addLocal(upDir);
    }
}
