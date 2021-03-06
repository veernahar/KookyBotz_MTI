package org.firstinspires.ftc.teamcode.opmode.test;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.roadrunner.profile.MotionProfile;
import com.acmerobotics.roadrunner.profile.MotionProfileGenerator;
import com.acmerobotics.roadrunner.profile.MotionState;
import com.arcrobotics.ftclib.controller.PIDController;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.common.hardware.Robot;

@Config
@TeleOp
@Disabled
public class Arm_Test extends OpMode {
    private Robot robot;
    private PIDController controller;
    private final double ticks_in_degree = 737 / 180.;

    public static int target = 0;
    private int previous_target = 0;

    public static double kcos = 0.2;

    public static double max_v = 8000;
    public static double max_a = 8000;

    public static double p, i, d;
    private double pp, pi, pd;

    private ElapsedTime time;

    private MotionProfile profile;

    @Override
    public void init() {
        robot = new Robot(hardwareMap, true);
        controller = new PIDController(p, i, d);
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
        time = new ElapsedTime();

        p = 0.01;
        d = 0.0005;
    }

    @Override
    public void loop() {
        if (p != pp || i != pi || d != pd) {
            controller.setPID(p, i, d);
        }

        if (target != previous_target) {
            profile = MotionProfileGenerator.generateSimpleMotionProfile(new MotionState(previous_target, 0), new MotionState(target, 0), max_v, max_a);
            time.reset();
            previous_target = target;
        }

        pp = p;
        pi = i;
        pd = d;

        int arm = robot.arm.pos();
        MotionState targetState = profile == null ? new MotionState(0, 0) : profile.get(time.seconds());
        double target = targetState.getX();
        double pid = controller.calculate(arm, target);
        double ff = Math.cos(Math.toRadians(target / ticks_in_degree)) * kcos;

        double power = (pid + ff) / robot.batteryVoltageSensor.getVoltage() * 12.0;

        robot.arm.arm.setPower(power);

        telemetry.addData("pos ", arm);
        telemetry.addData("target ", target);
        telemetry.addData("controller ", pid);
        telemetry.addData("ff ", ff);
        telemetry.addData("power ", power);
        telemetry.update();
    }
}
