package test1;

public class Math extends PMath {
        static Integer a1 = new Integer(10);
        Integer a = new Integer(10);
        public Math() {
            System.out.println(a);
        }

        @Override
        Integer geta() {
            return a;
        }
    }