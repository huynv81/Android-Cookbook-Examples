package vsf.news.mw.service;

import io.grpc.stub.StreamObserver;
import io.micronaut.grpc.annotation.GrpcService;
import vsf.chat.lib.grpc.GrpcUnary;
import vsf.news.proto.*;
import vsf.news.worker.handler.EngagementHandler;

@GrpcService
public class EngagementService extends EngagementServiceGrpc.EngagementServiceImplBase {

    private final EngagementHandler handler;

    public EngagementService(EngagementHandler handler) {
        this.handler = handler;
    }

    @Override
    public void doAction(EngagementRequest req, StreamObserver<BoolResponse> rsp) {
        GrpcUnary.run(rsp, req, () -> handler.doAction(req));
    }

    @Override
    public void undoAction(EngagementRequest req, StreamObserver<BoolResponse> rsp) {
        GrpcUnary.run(rsp, req, () -> handler.undoAction(req));
    }

    @Override
    public void isEngaged(IsEngagedRequest req, StreamObserver<BoolResponse> rsp) {
        GrpcUnary.run(rsp, req, () -> handler.isEngaged(req));
    }

    @Override
    public void getTotal(GetEngagementListRequest req, StreamObserver<TotalEngagementResponse> rsp) {
        GrpcUnary.run(rsp, req, () -> handler.getTotal(req));
    }

    @Override
    public void getListUser(GetEngagementListRequest req, StreamObserver<ListIdResponse> rsp) {
        GrpcUnary.run(rsp, req, () -> handler.getListUser(req));
    }

    @Override
    public void getListEntity(GetUserEngagementRequest req, StreamObserver<ListIdResponse> rsp) {
        GrpcUnary.run(rsp, req, () -> handler.getListEntity(req));
    }
}
