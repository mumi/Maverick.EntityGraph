package io.av360.maverick.graph.store.behaviours;

import io.av360.maverick.graph.model.rdf.NamespaceAwareStatement;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.sparqlbuilder.core.query.ModifyQuery;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface Searchable extends RepositoryBehaviour {

    default Flux<BindingSet> query(SelectQuery all, Authentication authentication, GrantedAuthority requiredAuthority) {
        return this.query(all.getQueryString(), authentication, requiredAuthority);
    }

    Flux<BindingSet> query(String queryString, Authentication authentication, GrantedAuthority requiredAuthority);

    //TODO delete this
    default Mono<Void> modify(ModifyQuery all, Authentication authentication, GrantedAuthority requiredAuthority) {
        return this.modify(all.getQueryString(), authentication, requiredAuthority);
    }
    Mono<Void> modify(String queryString, Authentication authentication, GrantedAuthority requiredAuthority);

    Flux<NamespaceAwareStatement> construct(String query, Authentication authentication, GrantedAuthority requiredAuthority);
}
