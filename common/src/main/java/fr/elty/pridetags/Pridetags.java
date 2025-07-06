package fr.elty.pridetags;

import fr.elty.pridetags.api.BaseAPI;
import fr.elty.pridetags.api.PronounsCC;
import fr.elty.pridetags.api.PronounsPage;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public final class Pridetags {

    public static final String MOD_ID = "pridetags";
    public static Path ConfigPath;

    public static final Thread THREAD = new Thread(Pridetags::asyncLoop);
    public static Queue<Consumer<Void>> queues = new ConcurrentLinkedQueue<>();
    public static AtomicBoolean finished = new AtomicBoolean(false);

    public static Set<Profile> profiles = ConcurrentHashMap.newKeySet();

    private static List<BaseAPI> apis = new ArrayList<>();

    public static void init() {
        apis.add(new PronounsPage());
        apis.add(new PronounsCC());
        THREAD.start();
    }

    public static Profile getProfile(String name) {
        Optional<Profile> maybeProfile = profiles.stream().filter(profile -> profile.getUsername().equals(name)).findFirst();
        if (maybeProfile.isPresent()) return maybeProfile.get();
        queues.add(v -> {
            if (profiles.stream().noneMatch(profile -> profile.getUsername().equals(name))) {
                for (BaseAPI api : Pridetags.apis) {
                    if (api.getAsyncProfile(name))
                        break;
                }
            }
        });
        return null;
    }

    private static void asyncLoop() {
        long lastTime = System.currentTimeMillis();
        while (!finished.get()) {
            while (!queues.isEmpty()) {
                Consumer<Void> consumer = queues.poll();
                consumer.accept(null);
            }
            if (lastTime + 600000 > System.currentTimeMillis()) continue;
            Set<Profile> newProfiles = new HashSet<>(Pridetags.profiles);
            Pridetags.profiles.clear();
            for (Profile profile : newProfiles) {
                for (BaseAPI api : apis) {
                    if (api.getAsyncProfile(profile.getUsername()))
                        break;
                }
            }
            lastTime = System.currentTimeMillis();
        }
    }
}
