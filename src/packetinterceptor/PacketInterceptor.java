package packetinterceptor;

import arc.Events;
import arc.func.Cons2;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.net.Net;
import mindustry.net.NetConnection;
import pluginutil.GHPlugin;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiPredicate;

import static mindustry.Vars.*;
import static pluginutil.PluginUtil.f;

@SuppressWarnings("unused")
public class PacketInterceptor extends GHPlugin {

    private LinkedHashMap<Class<?>, List<PIEntry>> listeners;

    public PacketInterceptor() {
        super();
        VERSION = "1.1";
    }

    @Override
    @SuppressWarnings("unchecked cast")
    public void init() {
        ObjectMap<Class<?>, Cons2<NetConnection, Object>> serverListeners;
        Field field;
        try {
            field = Net.class.getDeclaredField("serverListeners");
            field.setAccessible(true);
            serverListeners = (ObjectMap<Class<?>, Cons2<NetConnection, Object>>) field.get(Vars.net);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            return;
        }

        Seq<Class<?>> seq = serverListeners.keys().toSeq();
        listeners = new LinkedHashMap<>();
        for (int i = 0; i < seq.size; i++)
            listeners.put(seq.get(i), new ArrayList<>());

        for (ObjectMap.Entry<Class<?>, Cons2<NetConnection, Object>> serverListener : serverListeners) {
            final Class<?> cls = serverListener.key;
            final Cons2<NetConnection, Object> con2 = serverListener.value;
            net.handleServer(cls, (con, packet) -> {
                boolean overwrite = false;

                for (PIEntry entry : listeners.get(cls)){
                    try {
                        if (entry.pred.test(con, packet))
                            overwrite = true;
                    } catch (Exception e){
                        log(f("Error while executing interceptor. %n%s: %n%s",
                                entry.cls, Seq.with(e.getStackTrace()).toString("\n")));
                    }
                }

                if (overwrite) return;
                con2.get(con, packet);
            });
        }

        log(f("%s listener(s) modified.", serverListeners.size));
        Events.on(EventType.ServerLoadEvent.class, e -> {
            Events.fire(new PacketInterceptor());
            log("Interceptors loaded.");
        });
        log("Initialized\n");
    }

    @Override
    protected void defConfig() {}

    protected <T extends GHPluginConfig> T cfg() {
        return null;
    }

    public Class<?>[] getListeners(){
        return listeners.keySet().toArray(new Class[0]);
    }

    public void addListener(Class<?> cls, Class<?> from, BiPredicate<NetConnection, Object> pred){
        listeners.get(cls).add(new PIEntry(from, pred));
        log(f("Added Listener: to: %s, from: %s", cls.getSimpleName(), from.getSimpleName()));
    }

    private static class PIEntry{
        Class<?> cls;
        BiPredicate<NetConnection, Object> pred;

        public PIEntry(Class<?> cls, BiPredicate<NetConnection, Object> pred) {
            this.cls = cls;
            this.pred = pred;
        }
    }
}
