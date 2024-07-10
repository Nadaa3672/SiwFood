package it.uniroma3.siw.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.Cuoco;

public interface CredentialsRepository extends CrudRepository<Credentials, Long> {

	public Optional<Credentials> findByUsername(String username);

	public Credentials findByUser(Cuoco cuoco);
	
    @Query("SELECT c.user FROM Credentials c WHERE c.role <> 'ADMIN'")
    List<Cuoco> findNonAdminCuochi();
}
