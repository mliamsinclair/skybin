package proj.skybin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "server", ignoreUnknownFields = true)
public class ServerProperties {
    public static class Tomcat {
        public static class Threads {
            private int max = 200; // Maximum amount of worker threads

            public int getMax() {
                return max;
            }
        }
    }
}
