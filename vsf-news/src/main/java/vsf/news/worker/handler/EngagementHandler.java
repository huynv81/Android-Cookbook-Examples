package vsf.news.worker.handler;

import jakarta.inject.Singleton;
import vsf.chat.lib.grpc.GrpcErrors;
import vsf.news.proto.*;
import vsf.news.worker.cache.kvrocks.KvrEngagementCache;

import java.util.Set;

@Singleton
public class EngagementHandler {

    private final KvrEngagementCache cache;

    public EngagementHandler(KvrEngagementCache cache) {
        this.cache = cache;
    }

    public BoolResponse doAction(EngagementRequest req) {
        validate(req.getAction(), req.getEntityType(), req.getEntityId(), req.getUserId());
        
        String entityKey = cache.buildEntityKey(req.getActionValue(), req.getEntityTypeValue(), req.getEntityId());
        String userKey = cache.buildUserKey(req.getActionValue(), req.getEntityTypeValue(), req.getUserId());

        cache.addEngagement(entityKey, userKey, req.getUserId(), String.valueOf(req.getEntityId()));
        
        return BoolResponse.newBuilder().setSuccess(true).build();
    }

    public BoolResponse undoAction(EngagementRequest req) {
        validate(req.getAction(), req.getEntityType(), req.getEntityId(), req.getUserId());

        String entityKey = cache.buildEntityKey(req.getActionValue(), req.getEntityTypeValue(), req.getEntityId());
        String userKey = cache.buildUserKey(req.getActionValue(), req.getEntityTypeValue(), req.getUserId());

        cache.removeEngagement(entityKey, userKey, req.getUserId(), String.valueOf(req.getEntityId()));

        return BoolResponse.newBuilder().setSuccess(true).build();
    }

    public BoolResponse isEngaged(IsEngagedRequest req) {
        String key = cache.buildEntityKey(req.getActionValue(), req.getEntityTypeValue(), req.getEntityId());
        return BoolResponse.newBuilder().setSuccess(cache.checkExists(key, req.getUserId())).build();
    }

    public TotalEngagementResponse getTotal(GetEngagementListRequest req) {
        String key = cache.buildEntityKey(req.getActionValue(), req.getEntityTypeValue(), req.getEntityId());
        return TotalEngagementResponse.newBuilder().setTotal(cache.count(key)).build();
    }

    public ListIdResponse getListUser(GetEngagementListRequest req) {
        String key = cache.buildEntityKey(req.getActionValue(), req.getEntityTypeValue(), req.getEntityId());
        return fetchPaginated(key, req.getOffset(), req.getLimit());
    }

    public ListIdResponse getListEntity(GetUserEngagementRequest req) {
        String key = cache.buildUserKey(req.getActionValue(), req.getEntityTypeValue(), req.getUserId());
        return fetchPaginated(key, req.getOffset(), req.getLimit());
    }

    private ListIdResponse fetchPaginated(String key, int offset, int limit) {
        long total = cache.count(key);
        
        if (offset >= total) {
            return ListIdResponse.newBuilder()
                    .setOffset(offset)
                    .setTotal(total)
                    .setHasNext(false)
                    .build();
        }

        Set<String> ids = cache.getRange(key, offset, limit);
        
        int nextOffset = offset + ids.size();
        
        return ListIdResponse.newBuilder()
                .addAllIds(ids)
                .setOffset(offset)
                .setNextOffset(nextOffset)
                .setHasNext(nextOffset < total)
                .setTotal(total)
                .build();
    }

    private void validate(EngagementAction action, EntityType type, long eid, String uid) {
        if (action == EngagementAction.ENGAGEMENT_ACTION_UNSPECIFIED || 
            type == EntityType.UNSPECIFIED || 
            eid <= 0 || 
            uid == null || uid.isEmpty()) {
            throw GrpcErrors.invalidArg("Missing required fields or invalid enum type");
        }
    }
}
