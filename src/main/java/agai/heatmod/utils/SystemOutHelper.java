package agai.heatmod.utils;

public class SystemOutHelper {
    public static void printf(String format, Object... args){
        StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
        StackTraceElement caller2 = Thread.currentThread().getStackTrace()[3];
        System.out.println("Direct caller: " + caller.getClassName() + "." + caller.getMethodName() + " at line " + caller.getLineNumber());
        System.out.println("second caller: " + caller2.getClassName() + "." + caller2.getMethodName() + " at line " + caller2.getLineNumber());
        System.out.printf(format+"\n", args);
    }
    public static void printfplain(String format, Object... args){
       System.out.printf(format+"\n", args);
    }
    public static void printCallers(String format,int index, Object... args){
        for(int i=0;i<index;i++){
            StackTraceElement caller = Thread.currentThread().getStackTrace()[2+i];
            System.out.println(i+"th Caller "+caller.getClassName() + "." + caller.getMethodName() + " at line " + caller.getLineNumber());
        }
       System.out.printf(format+"\n", args);
    }
}
