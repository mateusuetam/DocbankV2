package docbank.core.services;

import docbank.core.domain.Usuario;
import docbank.core.ports.UsuarioDAO;
import docbank.core.utils.PasswordUtil;

import java.util.Arrays;
import java.util.List;

public class ServicoDeUsuarios {

    private final UsuarioDAO usuarioDAO;

    public ServicoDeUsuarios(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    public Usuario autenticar(String email, char[] senha) {
        if (email == null || email.trim().isEmpty() || senha == null || senha.length == 0) {
            throw new IllegalArgumentException("E-mail e senha são obrigatórios para autenticação.");
        }

        String senhaHash = PasswordUtil.hashSenha(senha);
        Usuario usuario = usuarioDAO.autenticar(email.trim(), senhaHash);

        if (usuario == null) {
            throw new IllegalArgumentException("Falha na autenticação. Verifique se suas credenciais estão corretas.");
        }
        if ("Suspenso".equalsIgnoreCase(usuario.getCargo())) {
            throw new IllegalStateException("Usuário suspenso. Acesso negado.");
        }
        return usuario;
    }

    public void cadastrarNovoUsuario(String nome, String email, char[] senha, String cargo) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome é obrigatório.");
        }

        String regexEmail = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (email == null || email.trim().isEmpty() || !email.trim().matches(regexEmail)) {
            throw new IllegalArgumentException("Um e-mail válido é obrigatório (ex: seu.nome@dominio.com).");
        }
        if (senha == null || senha.length == 0) {
            throw new IllegalArgumentException("A senha é obrigatória.");
        }
        if (senha.length < 8) {
            throw new IllegalArgumentException("A senha deve ter no mínimo 8 caracteres.");
        }
        if (cargo == null || cargo.trim().isEmpty()) {
            throw new IllegalArgumentException("O cargo do usuário é obrigatório.");
        }
        if (usuarioDAO.existeEmail(email.trim())) {
            throw new IllegalArgumentException("Este e-mail já está cadastrado. Tente outro.");
        }

        String senhaHash = PasswordUtil.hashSenha(senha);
        boolean sucesso = usuarioDAO.cadastrar(nome.trim(), email.trim(), senhaHash, cargo.trim());

        if (!sucesso) {
            throw new RuntimeException("Houve um erro interno ao tentar realizar o cadastro.");
        }
    }

    public List<Usuario> listarUsuarios() {
        return usuarioDAO.listarTodos();
    }

    public void alterarCargo(int idUser, String novoCargo) {
        if (idUser <= 0) {
            throw new IllegalArgumentException("ID de usuário inválido.");
        }
        if (novoCargo == null || novoCargo.trim().isEmpty()) {
            throw new IllegalArgumentException("O novo cargo é obrigatório.");
        }

        List<String> cargosPermitidos = Arrays.asList("Usuário", "Moderador", "Administrador", "Suspenso");
        if (!cargosPermitidos.contains(novoCargo.trim())) {
            throw new IllegalArgumentException("Cargo inválido. Cargos permitidos: " + cargosPermitidos);
        }

        boolean sucesso = usuarioDAO.alterarCargo(idUser, novoCargo.trim());
        if (!sucesso) {
            throw new IllegalStateException("Usuário não encontrado para alteração de cargo.");
        }
    }

    public void excluirUsuario(int idUser) {
        if (idUser <= 0) {
            throw new IllegalArgumentException("ID de usuário inválido.");
        }
        boolean sucesso = usuarioDAO.excluir(idUser);
        if (!sucesso) {
            throw new IllegalStateException("Usuário não encontrado para exclusão.");
        }
    }
}
