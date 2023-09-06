package test1;

public abstract class PMath {
    Integer a = new Integer(10);
     static Integer a2 = new Integer(10);
        public PMath(){
            System.out.println(geta());
        }
        abstract Integer geta();
    }