package nextstep.mvc.view;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import nextstep.mvc.view.exception.NotFoundViewException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JspView implements View {

    public static final String REDIRECT_PREFIX = "redirect:";
    private static final Logger log = LoggerFactory.getLogger(JspView.class);

    private final String viewName;

    public JspView(final String viewName) {
        this.viewName = viewName;
    }

    @Override
    public void render(final Map<String, ?> model, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        if (isRedirect()) {
            response.sendRedirect(viewName.substring(REDIRECT_PREFIX.length()));
            return;
        }

        model.keySet().forEach(key -> {
            log.debug("attribute name : {}, value : {}", key, model.get(key));
            request.setAttribute(key, model.get(key));
        });

        forwardToResource(request, response);
    }

    private boolean isRedirect() {
        return viewName.startsWith(REDIRECT_PREFIX);
    }

    private void forwardToResource(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            final RequestDispatcher requestDispatcher = request.getRequestDispatcher(viewName);
            requestDispatcher.forward(request, response);
        } catch (NullPointerException exception){
            throw new NotFoundViewException();
        }
    }
}
