package com.kyro.testing;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.SerializerFactory;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import com.esotericsoftware.minlog.Log;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class KryoTester {

    public static void main(String[] args) throws FileNotFoundException {

        Log.TRACE();
        Person person = new Person("A", "B");
        List<Person> persons = new ArrayList<>();
        persons.add(person);
        AdvancedOption advancedOption = new AdvancedOption("A");
        Company company = new Company(persons);
        AddOp addOp = new AddOp();

        try (FileOutputStream fos = new FileOutputStream("/Users/sperumalla/Documents/Learning/java/coding/ser.txt")) {
            try (Output output = new Output(fos)) {
                Kryo kryo = new Kryo();
                kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
                kryo.setDefaultSerializer(new SerializerFactory.ReflectionSerializerFactory(FieldSerializer.class));
                kryo.setDefaultSerializer(CompatibleFieldSerializer.class);
                kryo.register(Company.class);
                kryo.register(List.class);
                kryo.register(ArrayList.class);
                kryo.register(Person.class);
                kryo.register(Option.class);
                kryo.register(Op.class);
                kryo.setReferences(true);
                kryo.setRegistrationRequired(false);
                kryo.writeClassAndObject(output, company);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (FileInputStream fis = new FileInputStream("/Users/sperumalla/Documents/Learning/java/coding/ser.txt")) {

            try (Input input = new Input(fis)) {
                Kryo kryo = new Kryo();
                kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
                kryo.setDefaultSerializer(CompatibleFieldSerializer.class);
                kryo.register(Company.class);
                kryo.register(List.class);
                kryo.register(ArrayList.class);
                kryo.register(Person.class);
                kryo.register(Option.class);
                kryo.setReferences(true);
                kryo.setRegistrationRequired(false);
                Company c = (Company) kryo.readClassAndObject(input);
                System.out.println(c);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
