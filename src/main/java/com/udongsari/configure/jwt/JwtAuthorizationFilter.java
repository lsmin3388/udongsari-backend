package com.udongsari.configure.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.udongsari.account.entity.Account;
import com.udongsari.account.repository.AccountRepository;
import com.udongsari.configure.details.PrincipalDetails;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {
	private final AccountRepository accountRepository;

	public JwtAuthorizationFilter(
			AuthenticationManager authenticationManager,
			AccountRepository accountRepository) {
		super(authenticationManager);
		this.accountRepository = accountRepository;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		String authorization = request.getHeader(JwtProperties.HEADER_STRING);

		if (authorization == null || !authorization.startsWith(JwtProperties.TOKEN_PREFIX)) {
			chain.doFilter(request, response);
			return;
		}

		String token = authorization.replace(JwtProperties.TOKEN_PREFIX, "");

		String username = JWT.require(Algorithm.HMAC512(JwtProperties.SECRET)).build().verify(token)
				.getClaim("username").asString();

		if (username != null) {
			Optional<Account> userAccountOptional = accountRepository.findByUsername(username);

			if (userAccountOptional.isPresent()) {
				Account user = userAccountOptional.get();

				PrincipalDetails principalDetails = new PrincipalDetails(user);
				Authentication authentication = new UsernamePasswordAuthenticationToken(
						principalDetails,
						null,
						principalDetails.getAuthorities());

				SecurityContextHolder.getContext().setAuthentication(authentication);
			} else {
				throw new UsernameNotFoundException(" * 잘못된 JWT");
			}
		}

		chain.doFilter(request, response);
	}

}
