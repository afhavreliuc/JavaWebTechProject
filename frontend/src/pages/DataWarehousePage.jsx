import { useState, useEffect } from 'react';
import { propagationService } from '../services/api';
import { Database, RefreshCw, CheckCircle, AlertTriangle, BarChart3, TrendingUp, Users, PieChart } from 'lucide-react';
import { 
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer,
  BarChart, Bar, Cell, ComposedChart, Area
} from 'recharts';

export default function DataWarehousePage() {
  const [stats, setStats] = useState(null);
  const [financialData, setFinancialData] = useState([]);
  const [topDoctorsData, setTopDoctorsData] = useState([]);
  const [paretoData, setParetoData] = useState([]);
  const [recurrenceData, setRecurrenceData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [syncing, setSyncing] = useState(false);
  const [message, setMessage] = useState(null);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [statsRes, financialRes, doctorsRes, paretoRes, recurrenceRes] = await Promise.all([
        propagationService.getStats(),
        propagationService.getFinancialEvolution(),
        propagationService.getTopDoctors(),
        propagationService.getParetoAnalysis(),
        propagationService.getPatientRecurrence()
      ]);
      setStats(statsRes.data);
      setFinancialData(financialRes.data);
      setTopDoctorsData(doctorsRes.data);
      setParetoData(paretoRes.data);
      setRecurrenceData(recurrenceRes.data);
    } catch (error) {
      console.error('Error fetching DW data:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleSync = async () => {
    setSyncing(true);
    setMessage(null);
    try {
      const res = await propagationService.sync();
      setMessage({ type: 'success', text: res.data });
      fetchData();
    } catch (error) {
      setMessage({ type: 'error', text: 'Eroare la sincronizarea datelor.' });
    } finally {
      setSyncing(false);
    }
  };

  const handleSeedMock = async () => {
    setLoading(true);
    setMessage(null);
    try {
      const res = await propagationService.seedMock();
      setMessage({ type: 'success', text: res.data });
      fetchData();
    } catch (error) {
      setMessage({ type: 'error', text: 'Eroare la încărcarea datelor mock.' });
    } finally {
      setLoading(false);
    }
  };

  const monthNames = ["Ian", "Feb", "Mar", "Apr", "Mai", "Iun", "Iul", "Aug", "Sep", "Oct", "Noi", "Dec"];
  
  const formattedFinancialData = financialData.map(item => {
    const m = item.MONTH_NUM || item.month_num;
    const cy = item.CURRENT_YEAR || item.current_year || 0;
    const py = item.PREVIOUS_YEAR || item.previous_year || 0;
    
    return {
      name: monthNames[m - 1] || `Luna ${m}`,
      current: cy,
      previous: py
    };
  });

  const formattedTopDoctors = topDoctorsData.map(item => ({
    name: item.DOCTOR_NAME || item.doctor_name,
    revenue: item.TOTAL_REVENUE || item.total_revenue || 0,
    count: item.APPOINTMENT_COUNT || item.appointment_count || 0
  }));

  const formattedParetoData = paretoData.map((item, index) => {
    const venitMedic = item.VENIT_MEDIC || item.Venit_Medic || 0;
    const venitTotalClinica = item.VENIT_TOTAL_CLINICA || item.Venit_Total_Clinica || paretoData[0]?.VENIT_TOTAL_CLINICA || paretoData[0]?.Venit_Total_Clinica || 1;
    const procentDinTotal = item.PROCENT_DIN_TOTAL || item.Procent_Din_Total || 0;
    const venitCumulat = item.VENIT_CUMULAT || item.Venit_Cumulat || 0;
    const procentCumulat = venitTotalClinica > 0 ? (venitCumulat / venitTotalClinica) * 100 : 0;
    
    return {
      name: item.DOCTOR_NAME || item.doctor_name || 'Necunoscut',
      venitMedic: venitMedic,
      procentDinTotal: procentDinTotal,
      venitCumulat: venitCumulat,
      procentCumulat: Math.round(procentCumulat * 100) / 100,
      index: index + 1,
      isTop80: procentCumulat <= 80
    };
  });

  const formattedRecurrenceData = recurrenceData.map(item => ({
    pacient: item.PACIENT || item.Pacient,
    dataCurenta: item.DATA_VIZITA_CURENTA || item.Data_Vizita_Curenta,
    dataAnterioara: item.DATA_VIZITA_ANTERIOARA || item.Data_Vizita_Anterioara,
    zileIntre: item.ZILE_INTRE_VIZITE || item.Zile_Intre_Vizite
  }));

  const isDataValid = (table) => {
    if (!stats) return false;
    return stats[`oltp_${table}`] === stats[`dw_${table}`];
  };

  return (
    <div className="w-full space-y-8">
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
          <button 
            onClick={handleSeedMock}
            disabled={loading || syncing}
            className={`flex items-center gap-2 px-6 py-2 rounded-full font-bold text-white transition-all shadow-md ${
              loading || syncing ? 'bg-gray-400 cursor-not-allowed' : 'bg-green-600 hover:bg-green-700 active:scale-95'
            }`}
          >
            <Database size={20} />
            Încarcă Date Mock (Word)
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
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <div className="bg-white p-6 rounded-2xl shadow-lg border border-gray-100">
          <div className="flex items-center gap-2 mb-6">
            <TrendingUp className="text-indigo-600" size={24} />
            <h3 className="text-xl font-bold text-gray-800">Evoluție Financiară (Trend Analysis)</h3>
          </div>
          <div className="h-[300px] w-full">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={formattedFinancialData}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f0f0f0" />
                <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{fill: '#9ca3af', fontSize: 12}} />
                <YAxis axisLine={false} tickLine={false} tick={{fill: '#9ca3af', fontSize: 12}} />
                <Tooltip 
                  contentStyle={{borderRadius: '12px', border: 'none', boxShadow: '0 10px 15px -3px rgba(0,0,0,0.1)'}}
                />
                <Legend iconType="circle" />
                <Line 
                  type="monotone" 
                  dataKey="current" 
                  name="An Curent" 
                  stroke="#4f46e5" 
                  strokeWidth={3} 
                  dot={{ r: 4, fill: '#4f46e5' }}
                  activeDot={{ r: 6 }}
                />
                <Line 
                  type="monotone" 
                  dataKey="previous" 
                  name="An Anterior" 
                  stroke="#94a3b8" 
                  strokeWidth={2} 
                  strokeDasharray="5 5"
                  dot={{ r: 3, fill: '#94a3b8' }}
                />
              </LineChart>
            </ResponsiveContainer>
          </div>
          <p className="mt-4 text-xs text-gray-500 italic">
            * Comparație lunară a veniturilor totale între anul curent și cel precedent.
          </p>
        </div>

        <div className="bg-white p-6 rounded-2xl shadow-lg border border-gray-100">
          <div className="flex items-center gap-2 mb-6">
            <Users className="text-indigo-600" size={24} />
            <h3 className="text-xl font-bold text-gray-800">Top 5 Medici (Trimestrul Curent)</h3>
          </div>
          <div className="h-[300px] w-full">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={formattedTopDoctors} layout="vertical" margin={{ left: 20, right: 40 }}>
                <CartesianGrid strokeDasharray="3 3" horizontal={false} stroke="#f0f0f0" />
                <XAxis type="number" hide />
                <YAxis 
                  dataKey="name" 
                  type="category" 
                  axisLine={false} 
                  tickLine={false} 
                  tick={{fill: '#4b5563', fontSize: 12, fontWeight: 600}}
                  width={140}
                />
                <Tooltip 
                  cursor={{fill: '#f8fafc'}}
                  contentStyle={{borderRadius: '12px', border: 'none', boxShadow: '0 10px 15px -3px rgba(0,0,0,0.1)'}}
                />
                <Bar dataKey="revenue" name="Venit Total" fill="#4f46e5" radius={[0, 4, 4, 0]} barSize={20}>
                  {formattedTopDoctors.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={index === 0 ? '#4338ca' : '#6366f1'} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>
          <div className="mt-4 grid grid-cols-5 gap-2">
            {formattedTopDoctors.map((doc, idx) => (
              <div key={idx} className="text-center">
                <div className="text-[10px] font-bold text-gray-400 truncate">{doc.name}</div>
                <div className="text-xs font-bold text-indigo-600">{doc.count} prog.</div>
              </div>
            ))}
          </div>
        </div>
      </div>

      <div className="bg-white p-6 rounded-2xl shadow-lg border border-gray-100">
        <div className="flex items-center gap-2 mb-6">
          <PieChart className="text-indigo-600" size={24} />
          <h3 className="text-xl font-bold text-gray-800">Analiza Pareto a Veniturilor (Regula 80/20)</h3>
        </div>
        <p className="text-sm text-gray-600 mb-6 italic">
          Obiectiv: Vedem contribuția cumulată a medicilor la venitul total.
        </p>
        
        {formattedParetoData.length > 0 ? (
          <>
            <div className="h-[500px] w-full mb-6">
              <ResponsiveContainer width="100%" height="100%">
                <ComposedChart data={formattedParetoData} margin={{ top: 40, right: 100, left: 40, bottom: 120 }}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f0f0f0" />
                  <XAxis 
                    dataKey="name" 
                    angle={-45}
                    textAnchor="end"
                    height={100}
                    axisLine={false} 
                    tickLine={false} 
                    tick={{fill: '#6b7280', fontSize: 11}}
                    interval={0}
                    dy={20}
                  />
                  <YAxis 
                    yAxisId="left"
                    label={{ 
                      value: 'Venit (RON)', 
                      angle: -90, 
                      position: 'insideLeft', 
                      offset: -20,
                      style: { textAnchor: 'middle', fill: '#6b7280', fontWeight: 600 } 
                    }}
                    axisLine={false} 
                    tickLine={false} 
                    tick={{fill: '#9ca3af', fontSize: 12}}
                  />
                  <YAxis 
                    yAxisId="right"
                    orientation="right"
                    label={{ 
                      value: 'Procent Cumulat (%)', 
                      angle: 90, 
                      position: 'insideRight', 
                      offset: -20,
                      dx: 60,
                      style: { textAnchor: 'middle', fill: '#6b7280', fontWeight: 600 } 
                    }}
                    domain={[0, 100]}
                    axisLine={false} 
                    tickLine={false} 
                    tick={{fill: '#9ca3af', fontSize: 12}}
                  />
                  <Tooltip 
                    contentStyle={{borderRadius: '12px', border: 'none', boxShadow: '0 10px 15px -3px rgba(0,0,0,0.1)'}}
                    formatter={(value, name) => {
                      if (name === 'venitMedic') return [`${value.toLocaleString('ro-RO')} RON`, 'Venit Medic'];
                      if (name === 'procentCumulat') return [`${value.toFixed(2)}%`, 'Procent Cumulat'];
                      return [value, name];
                    }}
                  />
                  <Legend />
                  <Bar 
                    yAxisId="left"
                    dataKey="venitMedic" 
                    name="Venit Medic" 
                    fill="#4f46e5" 
                    radius={[4, 4, 0, 0]}
                  >
                    {formattedParetoData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.isTop80 ? '#4f46e5' : '#94a3b8'} />
                    ))}
                  </Bar>
                  <Area
                    yAxisId="right"
                    type="monotone"
                    dataKey="procentCumulat"
                    name="Procent Cumulat"
                    stroke="#ef4444"
                    fill="#fee2e2"
                    strokeWidth={2}
                  />
                  <Line
                    yAxisId="right"
                    type="monotone"
                    dataKey={() => 80}
                    stroke="#f59e0b"
                    strokeWidth={2}
                    strokeDasharray="5 5"
                    name="Prag 80%"
                    dot={false}
                  />
                </ComposedChart>
              </ResponsiveContainer>
            </div>

            <div className="overflow-x-auto">
              <table className="w-full border-collapse">
                <thead>
                  <tr className="bg-gray-50 border-b-2 border-gray-200">
                    <th className="text-left p-3 text-sm font-bold text-gray-700">#</th>
                    <th className="text-left p-3 text-sm font-bold text-gray-700">Nume Medic</th>
                    <th className="text-right p-3 text-sm font-bold text-gray-700">Venit Medic (RON)</th>
                    <th className="text-right p-3 text-sm font-bold text-gray-700">% din Total</th>
                    <th className="text-right p-3 text-sm font-bold text-gray-700">Venit Cumulat (RON)</th>
                    <th className="text-right p-3 text-sm font-bold text-gray-700">% Cumulat</th>
                  </tr>
                </thead>
                <tbody>
                  {formattedParetoData.map((item, index) => (
                    <tr 
                      key={index} 
                      className={`border-b border-gray-100 hover:bg-gray-50 ${
                        item.procentCumulat <= 80 ? 'bg-green-50' : ''
                      }`}
                    >
                      <td className="p-3 text-sm text-gray-600">{item.index}</td>
                      <td className="p-3 text-sm font-semibold text-gray-800">{item.name}</td>
                      <td className="p-3 text-sm text-right text-gray-700">
                        {item.venitMedic.toLocaleString('ro-RO', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} RON
                      </td>
                      <td className="p-3 text-sm text-right text-gray-700">
                        {item.procentDinTotal.toFixed(2)}%
                      </td>
                      <td className="p-3 text-sm text-right text-gray-700 font-semibold">
                        {item.venitCumulat.toLocaleString('ro-RO', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} RON
                      </td>
                      <td className={`p-3 text-sm text-right font-bold ${
                        item.procentCumulat <= 80 ? 'text-green-600' : 'text-gray-700'
                      }`}>
                        {item.procentCumulat.toFixed(2)}%
                        {item.procentCumulat <= 80 && index === formattedParetoData.findIndex(d => d.procentCumulat > 80) - 1 && (
                          <span className="ml-2 text-xs text-green-600">✓ Top 80%</span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
                {formattedParetoData.length > 0 && (
                  <tfoot>
                    <tr className="bg-indigo-50 border-t-2 border-indigo-200">
                      <td colSpan="2" className="p-3 text-sm font-bold text-gray-800">TOTAL CLINICĂ</td>
                      <td className="p-3 text-sm text-right font-bold text-indigo-700">
                        {formattedParetoData[0]?.venitCumulat ? 
                          formattedParetoData[formattedParetoData.length - 1].venitCumulat.toLocaleString('ro-RO', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + ' RON'
                          : '-'
                        }
                      </td>
                      <td className="p-3 text-sm text-right font-bold text-indigo-700">100.00%</td>
                      <td className="p-3 text-sm text-right font-bold text-indigo-700">
                        {formattedParetoData[0]?.venitCumulat ? 
                          formattedParetoData[formattedParetoData.length - 1].venitCumulat.toLocaleString('ro-RO', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + ' RON'
                          : '-'
                        }
                      </td>
                      <td className="p-3 text-sm text-right font-bold text-indigo-700">100.00%</td>
                    </tr>
                  </tfoot>
                )}
              </table>
            </div>
            <p className="mt-4 text-xs text-gray-500 italic">
              * Medici marcați cu verde contribuie la primii 80% din venitul total (Regula Pareto 80/20).
            </p>
          </>
        ) : (
          <div className="text-center py-12 text-gray-500">
            <AlertTriangle className="mx-auto mb-4" size={48} />
            <p>Nu există date disponibile pentru analiza Pareto.</p>
            <p className="text-sm mt-2">Asigură-te că ai sincronizat datele OLTP {'->'} DW.</p>
          </div>
        )}
      </div>

      <div className="bg-white p-6 rounded-2xl shadow-lg border border-gray-100">
        <div className="flex items-center gap-2 mb-6">
          <TrendingUp className="text-indigo-600" size={24} />
          <h3 className="text-xl font-bold text-gray-800">Analiza Recurenței Pacienților (Fidelizare)</h3>
        </div>

        {formattedRecurrenceData.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full border-collapse">
              <thead>
                <tr className="bg-gray-50 border-b-2 border-gray-200">
                  <th className="text-left p-3 text-sm font-bold text-gray-700">Pacient</th>
                  <th className="text-left p-3 text-sm font-bold text-gray-700">Data Vizită Curentă</th>
                  <th className="text-left p-3 text-sm font-bold text-gray-700">Data Vizită Anterioară</th>
                  <th className="text-right p-3 text-sm font-bold text-gray-700">Zile Între Vizite</th>
                </tr>
              </thead>
              <tbody>
                {formattedRecurrenceData.map((item, index) => (
                  <tr key={index} className="border-b border-gray-100 hover:bg-gray-50">
                    <td className="p-3 text-sm font-semibold text-gray-800">{item.pacient}</td>
                    <td className="p-3 text-sm text-gray-700">
                      {item.dataCurenta ? new Date(item.dataCurenta).toLocaleDateString('ro-RO') : '-'}
                    </td>
                    <td className="p-3 text-sm text-gray-700">
                      {item.dataAnterioara ? new Date(item.dataAnterioara).toLocaleDateString('ro-RO') : 'Prima vizită'}
                    </td>
    <td className="p-3 text-sm text-right font-bold text-indigo-600">
      {item.zileIntre !== null && item.zileIntre !== undefined ? `${item.zileIntre} zile` : '-'}
    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="text-center py-12 text-gray-500">
            <AlertTriangle className="mx-auto mb-4" size={48} />
            <p>Nu există date disponibile pentru analiza recurenței.</p>
            <p className="text-sm mt-2">Asigură-te că ai sincronizat datele OLTP {'->'} DW.</p>
          </div>
        )}
      </div>
    </div>
  );
}
