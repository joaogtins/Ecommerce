import { useState, type FormEvent } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Login() {
  const { login, register } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = (location.state as { from?: { pathname: string } })?.from?.pathname ?? '/';

  const [loginForm, setLoginForm] = useState({ email: '', password: '' });
  const [loginLoading, setLoginLoading] = useState(false);
  const [loginError, setLoginError] = useState<string | null>(null);

  const [registerForm, setRegisterForm] = useState({ name: '', email: '', password: '', phone: '' });
  const [registerLoading, setRegisterLoading] = useState(false);
  const [registerError, setRegisterError] = useState<string | null>(null);

  async function handleLogin(e: FormEvent) {
    e.preventDefault();
    setLoginLoading(true);
    setLoginError(null);
    try {
      await login(loginForm);
      navigate(from, { replace: true });
    } catch (err) {
      setLoginError(err instanceof Error ? err.message : 'Email ou senha inválidos.');
    } finally {
      setLoginLoading(false);
    }
  }

  async function handleRegister(e: FormEvent) {
    e.preventDefault();
    setRegisterLoading(true);
    setRegisterError(null);
    try {
      await register(registerForm);
      navigate(from, { replace: true });
    } catch (err) {
      setRegisterError(err instanceof Error ? err.message : 'Não foi possível criar sua conta.');
    } finally {
      setRegisterLoading(false);
    }
  }

  const inputClass =
    'w-full rounded-md border border-trie-200 px-3 py-2 text-sm text-ink focus:outline-none focus:ring-2 focus:ring-trie-400';

  return (
    <div className="max-w-4xl mx-auto px-4 py-14 grid sm:grid-cols-2 gap-8">
      <section className="bg-white border border-trie-100 rounded-xl p-8">
        <h1 className="font-display text-2xl text-ink mb-1">Bem-vindo de volta</h1>
        <p className="text-sm text-ink-soft mb-6">Acesse sua conta para ver seus pedidos e favoritos.</p>

        <form onSubmit={handleLogin} className="space-y-3">
          <div>
            <label className="text-xs text-ink-soft">E-mail</label>
            <input
              required
              type="email"
              className={inputClass}
              value={loginForm.email}
              onChange={(e) => setLoginForm((f) => ({ ...f, email: e.target.value }))}
            />
          </div>
          <div>
            <label className="text-xs text-ink-soft">Senha</label>
            <input
              required
              type="password"
              className={inputClass}
              value={loginForm.password}
              onChange={(e) => setLoginForm((f) => ({ ...f, password: e.target.value }))}
            />
          </div>

          {loginError && <p className="text-sm text-red-600">{loginError}</p>}

          <button
            type="submit"
            disabled={loginLoading}
            className="w-full rounded-full bg-trie-600 hover:bg-trie-700 disabled:bg-trie-300 text-white py-3 text-sm font-medium transition-colors"
          >
            {loginLoading ? 'Entrando…' : 'Entrar'}
          </button>
        </form>
      </section>

      <section className="bg-trie-800 text-white rounded-xl p-8">
        <h1 className="font-display text-2xl mb-1">Crie sua conta</h1>
        <p className="text-sm text-trie-200 mb-6">Leva menos de um minuto para pedir na Triê Pratas.</p>

        <form onSubmit={handleRegister} className="space-y-3">
          <div>
            <label className="text-xs text-trie-200">Nome</label>
            <input
              required
              className="w-full rounded-md bg-trie-700 border border-trie-600 px-3 py-2 text-sm text-white focus:outline-none focus:ring-2 focus:ring-trie-400"
              value={registerForm.name}
              onChange={(e) => setRegisterForm((f) => ({ ...f, name: e.target.value }))}
            />
          </div>
          <div>
            <label className="text-xs text-trie-200">E-mail</label>
            <input
              required
              type="email"
              className="w-full rounded-md bg-trie-700 border border-trie-600 px-3 py-2 text-sm text-white focus:outline-none focus:ring-2 focus:ring-trie-400"
              value={registerForm.email}
              onChange={(e) => setRegisterForm((f) => ({ ...f, email: e.target.value }))}
            />
          </div>
          <div>
            <label className="text-xs text-trie-200">Senha</label>
            <input
              required
              minLength={6}
              type="password"
              className="w-full rounded-md bg-trie-700 border border-trie-600 px-3 py-2 text-sm text-white focus:outline-none focus:ring-2 focus:ring-trie-400"
              value={registerForm.password}
              onChange={(e) => setRegisterForm((f) => ({ ...f, password: e.target.value }))}
            />
          </div>

          {registerError && <p className="text-sm text-red-300">{registerError}</p>}

          <button
            type="submit"
            disabled={registerLoading}
            className="w-full rounded-full bg-trie-400 hover:bg-trie-300 disabled:bg-trie-600 text-trie-900 py-3 text-sm font-medium transition-colors"
          >
            {registerLoading ? 'Criando conta…' : 'Cadastrar'}
          </button>
        </form>
      </section>
    </div>
  );
}
