package com.buscarm.registro.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.buscarm.registro.application.port.in.CadastrarRegistroUseCase.CadastrarRegistroCommand;
import com.buscarm.registro.application.port.out.RegistroRepositoryPort;
import com.buscarm.registro.application.port.out.UsuarioAtualPort;
import com.buscarm.registro.application.port.out.UsuarioSessaoPort;
import com.buscarm.registro.domain.Registro;
import com.buscarm.registro.domain.exception.RegistroDuplicadoException;

/**
 * Teste da camada de aplicação SEM Spring nem banco — apenas mocks das portas. É o ganho central
 * da arquitetura hexagonal: as regras são exercitáveis isoladamente.
 */
@ExtendWith(MockitoExtension.class)
class RegistroApplicationServiceTest {

    @Mock
    private RegistroRepositoryPort repositorio;

    @Mock
    private UsuarioAtualPort usuarioAtual;

    @Mock
    private UsuarioSessaoPort usuarioSessao;

    @InjectMocks
    private RegistroApplicationService service;

    @Test
    void cadastraDerivandoSobrenomeEDefinindoUsuarioDaSessao() {
        when(usuarioAtual.getUsuarioAtual()).thenReturn("secretaria");
        when(repositorio.existePorRm(anyInt())).thenReturn(false);
        when(repositorio.salvar(any(Registro.class))).thenAnswer(inv -> inv.getArgument(0));

        Registro salvo = service.cadastrar(
                new CadastrarRegistroCommand(203, "Sousa, João Victor Cosme Neres de", null));

        assertThat(salvo.getSobrenome()).isEqualTo("Sousa");
        assertThat(salvo.getNomeBusca()).isEqualTo("sousa, joao victor cosme neres de");
        verify(usuarioSessao).definirUsuarioSessao("secretaria");
    }

    @Test
    void rejeitaRmDuplicado() {
        when(usuarioAtual.getUsuarioAtual()).thenReturn("secretaria");
        when(repositorio.existePorRm(503)).thenReturn(true);

        assertThatThrownBy(() ->
                service.cadastrar(new CadastrarRegistroCommand(503, "Souza, Lara", null)))
                .isInstanceOf(RegistroDuplicadoException.class);
    }
}
