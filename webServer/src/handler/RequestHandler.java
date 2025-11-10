package handler;

import core.HttpRequest;
import core.HttpResponse;

public interface RequestHandler {

    HttpResponse handle(HttpRequest request);
}
