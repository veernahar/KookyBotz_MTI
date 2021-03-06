package org.firstinspires.ftc.teamcode.common.hardware;

import com.acmerobotics.dashboard.config.Config;
import com.arcrobotics.ftclib.drivebase.DifferentialDrive;
import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.arcrobotics.ftclib.hardware.motors.MotorEx;
import com.arcrobotics.ftclib.hardware.motors.MotorGroup;
import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.rev.Rev2mDistanceSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.VoltageSensor;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.teamcode.common.commandbase.subsytem.Arm;
import org.firstinspires.ftc.teamcode.common.commandbase.subsytem.Bucket;
import org.firstinspires.ftc.teamcode.common.commandbase.subsytem.Intake;
import org.firstinspires.ftc.teamcode.common.commandbase.subsytem.Turret;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvWebcam;
import org.outoftheboxrobotics.neutrinoi2c.Rev2mDistanceSensor.AsyncRev2MSensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Config
public class Robot {
    public DifferentialDrive drive;
    public MotorGroup left;
    public MotorGroup right;
    public MotorEx left_back, left_middle, left_front, right_back, right_middle, right_front, i;
    public DcMotorEx a;
    public Motor.Encoder right_encoder;
    public Motor.Encoder left_encoder;
    public BNO055IMU imu;
    public VoltageSensor batteryVoltageSensor;

    public Turret turret;

    public Arm arm;

    public Bucket bucket;

    public Intake intake;

    public OpenCvWebcam webcam, webcam2;


    public static double MAX_CURRENT = 15;
    private final List<LynxModule> hubs;


    public Robot(HardwareMap hardwareMap, boolean camera) {
        left_back = new MotorEx(hardwareMap, "lb", Motor.GoBILDA.RPM_435);
        left_middle = new MotorEx(hardwareMap, "lm", Motor.GoBILDA.RPM_435);
        left_front = new MotorEx(hardwareMap, "lf", Motor.GoBILDA.RPM_435);

        left_back.setZeroPowerBehavior(Motor.ZeroPowerBehavior.BRAKE);
        left_middle.setZeroPowerBehavior(Motor.ZeroPowerBehavior.BRAKE);
        left_front.setZeroPowerBehavior(Motor.ZeroPowerBehavior.BRAKE);

        left_middle.motorEx.setDirection(DcMotorSimple.Direction.REVERSE);

        left = new MotorGroup(left_back, left_middle, left_front);

        right_back = new MotorEx(hardwareMap, "rb", Motor.GoBILDA.RPM_435);
        right_middle = new MotorEx(hardwareMap, "rm", Motor.GoBILDA.RPM_435);
        right_front = new MotorEx(hardwareMap, "rf", Motor.GoBILDA.RPM_435);

        right_back.setZeroPowerBehavior(Motor.ZeroPowerBehavior.BRAKE);
        right_middle.setZeroPowerBehavior(Motor.ZeroPowerBehavior.BRAKE);
        right_front.setZeroPowerBehavior(Motor.ZeroPowerBehavior.BRAKE);

        right_middle.motorEx.setDirection(DcMotorSimple.Direction.REVERSE);

        right = new MotorGroup(right_back, right_middle, right_front);


        drive = new DifferentialDrive(left, right);

        imu = hardwareMap.get(BNO055IMU.class, "imu");
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit = BNO055IMU.AngleUnit.RADIANS;
        imu.initialize(parameters);

        right_encoder = right_front.encoder;
        right_encoder.setDirection(Motor.Direction.REVERSE);
        left_encoder = left_middle.encoder;

        right_encoder.reset();
        left_encoder.reset();

        batteryVoltageSensor = hardwareMap.voltageSensor.iterator().next();

        Servo left = hardwareMap.get(Servo.class, "left");
        Servo right = hardwareMap.get(Servo.class, "right");

        turret = new Turret(left, right);

        a = hardwareMap.get(DcMotorEx.class, "arm");
        a.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        Servo l = hardwareMap.get(Servo.class, "linkage");

        arm = new Arm(a, l, batteryVoltageSensor);

        i = new MotorEx(hardwareMap, "intake", Motor.GoBILDA.RPM_1150);
        i.motorEx.setDirection(DcMotorSimple.Direction.REVERSE);
        i.motorEx.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        intake = new Intake(i);

        Servo d = hardwareMap.get(Servo.class, "dump");
        Servo g = hardwareMap.get(Servo.class, "gate");

        Rev2mDistanceSensor ds = hardwareMap.get(Rev2mDistanceSensor.class, "distance");
        AsyncRev2MSensor asyncSensor = new AsyncRev2MSensor(ds);
        asyncSensor.setSensorAccuracyMode(AsyncRev2MSensor.AccuracyMode.MODE_HIGH_SPEED);

        bucket = new Bucket(d, g, asyncSensor);

        for (LynxModule hub : hubs = hardwareMap.getAll(LynxModule.class)) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }

        if (camera) {
            int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
            webcam = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, "Webcam 1"), cameraMonitorViewId);
            webcam2 = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, "Webcam 2"));
        }
    }

    public void currentUpdate(Telemetry telemetry) {
        List<Double> current_list = new ArrayList<>();
        current_list.add(left_back.motorEx.getCurrent(CurrentUnit.AMPS));
        current_list.add(left_middle.motorEx.getCurrent(CurrentUnit.AMPS));
        current_list.add(left_front.motorEx.getCurrent(CurrentUnit.AMPS));
        current_list.add(right_back.motorEx.getCurrent(CurrentUnit.AMPS));
        current_list.add(right_middle.motorEx.getCurrent(CurrentUnit.AMPS));
        current_list.add(right_front.motorEx.getCurrent(CurrentUnit.AMPS));
        current_list.add(i.motorEx.getCurrent(CurrentUnit.AMPS));
        current_list.add(a.getCurrent(CurrentUnit.AMPS));

        telemetry.addLine(String.format(Locale.ENGLISH, "left: %.2f, %.2f, %.2f right: %.2f, %.2f, %.2f intake: %.2f arm: %.2f",
                current_list.get(0),
                current_list.get(1),
                current_list.get(2),
                current_list.get(3),
                current_list.get(4),
                current_list.get(5),
                current_list.get(6),
                current_list.get(7)
        ));

        double current = 0;

        for (LynxModule hub : hubs) {
            current += hub.getCurrent(CurrentUnit.AMPS);
        }

        telemetry.addData("total current: ", current);
    }
}
