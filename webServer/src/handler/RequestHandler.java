package handler;

import core.HttpRequest;
import core.HttpResponse;

/**
 * Request Handler Interface
 */
public interface RequestHandler {

    HttpResponse handle(HttpRequest request) throws Exception;
}
