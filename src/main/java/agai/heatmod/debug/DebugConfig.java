package agai.heatmod.debug;

public class DebugConfig {
    public static boolean ENABLE_HEAT_DEBUG = true;
    public static enumDebug debugType=enumDebug.CHUNKCAPA;
    public static <T> void debug(enumDebug TYPE,funcDebug<T> debugFunc){
        if(ENABLE_HEAT_DEBUG&&debugType==TYPE){
            debugFunc.accept(TYPE);
        }
    }
    public static <T> void runtime(funcRunTime<T> runTimeFunc){
        if(!ENABLE_HEAT_DEBUG){
            runTimeFunc.accept();
        }
    }
    public enum enumDebug{
        CHUNKCAPA,
        NONE;
    }
    @FunctionalInterface
    public interface funcDebug<T>{
        void accept(enumDebug TYPE);
    }
    @FunctionalInterface
    public interface funcRunTime<T>{
        void accept();
    }
}
