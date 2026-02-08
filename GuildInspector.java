import net.dv8tion.jda.api.entities.Guild;
import java.lang.reflect.Method;

public class GuildInspector {
    public static void main(String[] args) {
        try {
            Class<?> clazz = Guild.class;
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
