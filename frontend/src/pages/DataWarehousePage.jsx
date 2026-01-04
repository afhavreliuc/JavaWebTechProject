import { useState, useEffect } from 'react';
import { propagationService } from '../services/api';
import { Database, RefreshCw, CheckCircle, AlertTriangle, BarChart3 } from 'lucide-react';

export default function DataWarehousePage() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(false);
  const [syncing, setSyncing] = useState(false);
  const [message, setMessage] = useState(null);

  const fetchStats = async () => {
    setLoading(true);
    try {
      const res = await propagationService.getStats();
      setStats(res.data);
    } catch (error) {
      console.error('Error fetching stats:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStats();
  }, []);

  const handleSync = async () => {
    setSyncing(true);
    setMessage(null);
    try {
      const res = await propagationService.sync();
      setMessage({ type: 'success', text: res.data });
      fetchStats();
    } catch (error) {
      setMessage({ type: 'error', text: 'Eroare la sincronizarea datelor.' });
    } finally {
      setSyncing(false);
    }
  };

  const isDataValid = (table) => {
    if (!stats) return false;
    return stats[`oltp_${table}`] === stats[`dw_${table}`];
  };

  return (
    <div className="max-w-4xl mx-auto">
      <div className="bg-white p-8 rounded-2xl shadow-lg border border-gray-100">
        <div className="flex justify-between items-center mb-8">
          <h2 className="text-3xl font-bold text-gray-800 flex items-center gap-3">
            <Database className="text-indigo-600" size={32} />
            Data Warehouse (DW)
          </h2>
          <button 
            onClick={handleSync}
            disabled={syncing}
            className={`flex items-center gap-2 px-6 py-2 rounded-full font-bold text-white transition-all shadow-md ${
              syncing ? 'bg-gray-400 cursor-not-allowed' : 'bg-indigo-600 hover:bg-indigo-700 active:scale-95'
            }`}
          >
            <RefreshCw size={20} className={syncing ? 'animate-spin' : ''} />
            {syncing ? 'Se sincronizează...' : 'Sincronizează OLTP -> DW'}
          </button>
        </div>

        {message && (
          <div className={`mb-6 p-4 rounded-xl flex items-center gap-3 ${
            message.type === 'success' ? 'bg-green-50 text-green-700' : 'bg-red-50 text-red-700'
          }`}>
            {message.type === 'success' ? <CheckCircle /> : <AlertTriangle />}
            {message.text}
          </div>
        )}

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {['patients', 'doctors', 'appointments'].map((table) => (
            <div key={table} className="bg-gray-50 p-6 rounded-2xl border border-gray-100 flex flex-col items-center">
              <h3 className="text-lg font-bold text-gray-700 capitalize mb-4">{table}</h3>
              <div className="flex justify-between w-full mb-2">
                <span className="text-sm text-gray-500">OLTP (Sursă):</span>
                <span className="font-bold text-indigo-600">{stats ? stats[`oltp_${table}`] : '-'}</span>
              </div>
              <div className="flex justify-between w-full mb-4">
                <span className="text-sm text-gray-500">DW (Destinație):</span>
                <span className="font-bold text-indigo-600">{stats ? stats[`dw_${table}`] : '-'}</span>
              </div>
              
              <div className={`w-full py-2 px-4 rounded-lg flex items-center justify-center gap-2 text-xs font-bold ${
                isDataValid(table) ? 'bg-green-100 text-green-700' : 'bg-amber-100 text-amber-700'
              }`}>
                {isDataValid(table) ? (
                  <><CheckCircle size={14} /> Date Validate</>
                ) : (
                  <><AlertTriangle size={14} /> Sincronizare Necesară</>
                )}
              </div>
            </div>
          ))}
        </div>

        {stats?.error && (
          <div className="mt-8 p-4 bg-amber-50 text-amber-800 rounded-xl flex items-center gap-3 border border-amber-200">
            <AlertTriangle size={24} />
            <p className="text-sm">{stats.error}</p>
          </div>
        )}

        <div className="mt-12 p-6 bg-indigo-50 rounded-2xl border border-indigo-100">
          <h4 className="text-indigo-900 font-bold mb-2 flex items-center gap-2">
            <BarChart3 size={18} />
            Informații ETL (Extract, Transform, Load)
          </h4>
          <p className="text-indigo-700 text-sm leading-relaxed">
            Acest modul permite transferul datelor tranzacționale (OLTP) către baza de date analitică (DW). 
            Procesul de propagare curăță datele din DW și le reîncarcă din sursa Medical, asigurând 
            integritatea și disponibilitatea lor pentru raportare fără a afecta performanța sistemului principal.
          </p>
        </div>
      </div>
    </div>
  );
}

