import net.dv8tion.jda.api.entities.Member;
import java.lang.reflect.Method;

public class MemberInspector {
    public static void main(String[] args) {
        try {
            Class<?> clazz = Member.class;
            System.out.println("Methods in " + clazz.getName() + ":");
            for (Method method : clazz.getMethods()) {
                if (method.getName().toLowerCase().contains("boost")) {
                    System.out.println(method.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
