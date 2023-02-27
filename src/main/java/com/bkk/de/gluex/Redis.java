package com.bkk.de.gluex;

import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;
import java.util.Set;

@Component
public class Redis {

    JedisPool pool;

    public Redis() {
        pool = new JedisPool("localhost", 6379);
    }

    public void write(String key, String value) {
        try (Jedis jedis = pool.getResource()) {
            jedis.set(key, value);
        }
    }

    public Set<String> getKeys(String key) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.keys(key);
        }
    }


    public String getKey(String key) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.get(key);
        }
    }
}
