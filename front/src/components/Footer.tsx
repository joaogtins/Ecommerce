import { useState, type FormEvent } from 'react';

export default function Footer() {
  const [sent, setSent] = useState(false);

  function handleSubmit(e: FormEvent) {
    e.preventDefault();
    // A API ainda não expõe um endpoint de suporte/contato — feedback local por ora.
    setSent(true);
  }

  return (
    <footer className="bg-trie-800 text-trie-100 mt-16">
      <div className="max-w-6xl mx-auto px-4 py-10 grid gap-8 sm:grid-cols-3">
        <div>
          <span className="w-9 h-9 rounded-full bg-trie-500 text-white grid place-items-center font-display text-sm">
            TP
          </span>
          <p className="mt-3 text-sm text-trie-200">
            Joias em prata 925, feitas para durar. Peças autorais e edições limitadas.
          </p>
        </div>

        <div>
          <h3 className="text-sm font-semibold text-white mb-3">Informações</h3>
          <ul className="space-y-2 text-sm text-trie-200">
            <li>Política de Troca</li>
            <li>Garantia e Contatos</li>
            <li>Trocas e Entrega</li>
          </ul>
        </div>

        <div>
          <h3 className="text-sm font-semibold text-white mb-3">Algum problema com seu pedido?</h3>
          {sent ? (
            <p className="text-sm text-trie-200">Recebemos sua mensagem, obrigado!</p>
          ) : (
            <form onSubmit={handleSubmit} className="space-y-2">
              <input
                required
                type="email"
                placeholder="Seu email"
                className="w-full rounded-md bg-trie-700 border border-trie-600 px-3 py-2 text-sm text-white placeholder:text-trie-300 focus:outline-none focus:ring-2 focus:ring-trie-400"
              />
              <textarea
                required
                placeholder="Conte-nos o que aconteceu"
                rows={2}
                className="w-full rounded-md bg-trie-700 border border-trie-600 px-3 py-2 text-sm text-white placeholder:text-trie-300 focus:outline-none focus:ring-2 focus:ring-trie-400"
              />
              <button
                type="submit"
                className="w-full rounded-md bg-trie-400 hover:bg-trie-300 text-trie-900 text-sm font-medium py-2 transition-colors"
              >
                Enviar
              </button>
            </form>
          )}
        </div>
      </div>
      <div className="border-t border-trie-700 text-center text-xs text-trie-300 py-4">
        © {new Date().getFullYear()} Triê Pratas. Todos os direitos reservados.
      </div>
    </footer>
  );
}
