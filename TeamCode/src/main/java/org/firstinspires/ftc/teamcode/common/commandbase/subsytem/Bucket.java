package org.firstinspires.ftc.teamcode.common.commandbase.subsytem;

import com.acmerobotics.dashboard.config.Config;
import com.arcrobotics.ftclib.command.SubsystemBase;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

@Config
public class Bucket extends SubsystemBase {
    private final Servo dump;
    private final Servo gate;
    private final DistanceSensor distance;

    public static double in_position = 0.7;
    public static double rest_position = 0.6;
    public static double dump_position = 0.9;

    public static double gate_closed = 0.6;
    public static double gate_open = 1;

    public Bucket(Servo d, Servo g, DistanceSensor ds) {
        dump = d;
        gate = g;
        distance = ds;
    }

    public void in() {
        dump.setPosition(in_position);
    }

    public void rest() {
        dump.setPosition(rest_position);
    }

    public void open() {
        gate.setPosition(gate_open);
    }

    public void close() {
        gate.setPosition(gate_closed);
    }

    public boolean hasFreight() {
        return distance.getDistance(DistanceUnit.CM) < 5.5;
    }

    public void dump() {
        dump.setPosition(dump_position);
    }
}
