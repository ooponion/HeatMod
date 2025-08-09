package agai.heatmod.annotators;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.objectweb.asm.Type;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class DocsGenerator {
    private static final List<Class<?>> classes = new ArrayList<>();
    private static Type ANNO_TYPE;
    private static Class<? extends Annotation> ANNO_CLAZZ;

    private static void genDoc(String dirName,String name,Class<? extends Annotation> clazz){
        try {
            System.out.println("api-summary will be generated in path: " + dirName);
            ANNO_TYPE= Type.getType(clazz);
            ANNO_CLAZZ=clazz;
            NewScan();
            generateDocsToDirectory(dirName,name);

            System.out.println("文档生成完成！共处理 " + classes.size() + " 个带@"+ANNO_TYPE.getClassName()+"注解的类");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void NewScan(){
        classes.clear();
        List<Class<?>> testClasses = ModList.get().getAllScanData().stream()
                .flatMap(scanData -> scanData.getAnnotations().stream())
                .filter(anno -> ANNO_TYPE.equals(anno.annotationType()))
                .map(anno -> {
                    try {
                        return Class.forName(anno.clazz().getClassName());
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
        classes.addAll(testClasses);
    }

    private static void generateDocsToDirectory(String outputDir,String name) throws IOException {
        // 创建输出目录（如果不存在）
        Path outputPath = Paths.get(outputDir);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }

        // 生成汇总文档
        Path summaryPath = outputPath.resolve(name+".md");
        try (BufferedWriter writer = Files.newBufferedWriter(summaryPath)) {
            writer.write("#文档汇总\n\n");
            writer.write("本文档自动生成，包含所有标记了@"+ANNO_TYPE.getClassName()+"注解的类\n\n");
            writer.write("生成时间: " + new Date() + "\n\n");
            writer.write("共 " + classes.size() + " 个带注解的类\n\n");
            for (Class<?> clazz : classes) {
                writeClassDoc(clazz, writer);

            }
        }
    }

    private static void writeClassDoc(Class<?> clazz, BufferedWriter writer) throws IOException {
        writer.write("## " + clazz.getSimpleName() + "\n");
        writer.write("- 类名：" + clazz.getName() + "\n\n");
        Annotation annotation= clazz.getAnnotation(ANNO_CLAZZ);
        writeAnoValues(annotation,writer);
        for (Method method : clazz.getDeclaredMethods()) {

            if (method.isAnnotationPresent(ANNO_CLAZZ)) {
                annotation= method.getAnnotation(ANNO_CLAZZ);
                writer.write("#### 方法" + method.getName() + "\n");
                writeAnoValues(annotation,writer);
                writer.write("--\n\n");
            }
        }
        writer.write("---\n\n");
    }
    private static void writeAnoValues(Annotation annotation,BufferedWriter writer) throws IOException {
        if(ANNO_CLAZZ.equals(ApiDoc.class)){
            String description   =((ApiDoc)annotation).description();
            writer.write(description+ "\n");
        }
    }

    public static void genInTestDoc(){
        genDoc("Intest-docs","intestClasses",InTest.class);
    }
    public static void genApiDoc(){
        genDoc("generated-docs","api-summary",ApiDoc.class);
    }
    public static void genInWorkindDoc(){
        genDoc("Inworking-docs","inworkingClasses",InWorking.class);
    }
    public static void genDocs(){
        genInTestDoc();
        genInWorkindDoc();
        genApiDoc();
    }
}
