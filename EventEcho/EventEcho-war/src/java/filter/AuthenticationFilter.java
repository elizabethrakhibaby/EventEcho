package filter;

import java.io.IOException;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import managedbean.AuthenticationManagedBean;

public class AuthenticationFilter implements Filter {

    @Inject
    private AuthenticationManagedBean authenticationManagedBean;

    public AuthenticationFilter() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String requestURI = httpRequest.getRequestURI();

        // Check if request is for login page or other public resources
        if (requestURI.endsWith("login.xhtml") || isPublicResource(requestURI)) {
            chain.doFilter(request, response); // Continue without redirecting
        } else {
            // Your existing login check logic here
            if (authenticationManagedBean == null || authenticationManagedBean.getPersonId() == -1L) {
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.xhtml");
            } else {
                chain.doFilter(request, response); // User is logged in, continue
            }
        }
    }

    private boolean isPublicResource(String uri) {
        // Implement logic to determine if the URI is for a public resource
        // Example: return uri.endsWith(".js") || uri.endsWith(".css");
        //return false; // Modify this based on your application's public resources
        return true;
        //return uri.endsWith("login.xhtml") || uri.endsWith("register.xhtml") || uri.endsWith(".js") || uri.endsWith(".css");
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        //do nothing
    }

    @Override
    public void destroy() {
        //do nothing
    }
}
