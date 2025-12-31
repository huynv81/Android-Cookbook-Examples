package vsf.news.worker.cache.kvrocks;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import vsf.chat.lib.cache.redis.client.RedisSetClient;
import vsf.chat.lib.cache.redis.commands.RedisCommand;
import java.util.Set;

@Singleton
public class KvrEngagementCache {

    private final RedisCommand cmd;

    @Inject
    public KvrEngagementCache(@Named("kvr-command") RedisCommand cmd) {
        this.cmd = cmd;
    }

    public String buildEntityKey(int action, int type, long eid) {
        return String.format("act:%d:%d:%d", action, type, eid);
    }

    public String buildUserKey(int action, int type, String uid) {
        return String.format("u:act:%d:%d:%s", action, type, uid);
    }

    public void addEngagement(String entityKey, String userKey, String userId, String entityIdStr) {
        long score = System.currentTimeMillis();
        cmd.zadd(entityKey, score, userId);
        cmd.zadd(userKey, score, entityIdStr);
    }

    public void removeEngagement(String entityKey, String userKey, String userId, String entityIdStr) {
        cmd.zrem(entityKey, userId);
        cmd.zrem(userKey, entityIdStr);
    }

    public boolean checkExists(String key, String member) {
        return cmd.zscore(key, member) != null;
    }

    public long count(String key) {
        return cmd.zcard(key);
    }

    public Set<String> getRange(String key, int offset, int limit) {
        return cmd.zrevrange(key, offset, offset + limit - 1);
    }
}
