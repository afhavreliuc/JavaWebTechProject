import { useState, useEffect } from 'react';
import { patientService, medicalServiceService, appointmentService } from '../services/api';
import { Calendar, Clock, CheckCircle2, AlertCircle } from 'lucide-react';

export default function AppointmentsPage() {
  const [patients, setPatients] = useState([]);
  const [services, setServices] = useState([]);
  const [selectedPatientId, setSelectedPatientId] = useState('');
  const [patientAppointments, setPatientAppointments] = useState([]);
  
  const [bookingMode, setBookingMode] = useState(false);
  const [bookingData, setBookingData] = useState({
    medicalServiceId: '',
    date: new Date().toISOString().split('T')[0],
    timeSlot: ''
  });
  const [availableSlots, setAvailableSlots] = useState([]);
  const [loadingSlots, setLoadingSlots] = useState(false);
  const [message, setMessage] = useState(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [pRes, sRes] = await Promise.all([
          patientService.getAll(),
          medicalServiceService.getAll()
        ]);
        setPatients(pRes.data);
        // Filtrăm doar serviciile care au cel puțin un doctor alocat
        const servicesWithDoctors = sRes.data.filter(s => s.medicalServiceDoctors && s.medicalServiceDoctors.length > 0);
        setServices(servicesWithDoctors);
      } catch (error) {
        console.error('Error fetching initial data:', error);
      }
    };
    fetchData();
  }, []);

  useEffect(() => {
    if (selectedPatientId) {
      fetchPatientAppointments(selectedPatientId);
    } else {
      setPatientAppointments([]);
    }
  }, [selectedPatientId]);

  const fetchPatientAppointments = async (id) => {
    try {
      const res = await appointmentService.getByPatientId(id);
      setPatientAppointments(res.data);
    } catch (error) {
      console.error('Error fetching appointments:', error);
    }
  };

  const handleFetchSlots = async () => {
    if (!bookingData.medicalServiceId || !bookingData.date) return;
    setLoadingSlots(true);
    try {
      const res = await appointmentService.getAvailableSlots(
        bookingData.medicalServiceId, 
        bookingData.date
      );
      setAvailableSlots(res.data);
    } catch (error) {
      console.error('Error fetching slots:', error);
    } finally {
      setLoadingSlots(false);
    }
  };

  const handleBook = async () => {
    if (!selectedPatientId || !bookingData.timeSlot) return;
    try {
      await appointmentService.create({
        patientId: selectedPatientId,
        medicalServiceId: bookingData.medicalServiceId,
        appointmentFrom: bookingData.timeSlot
      });
      setMessage({ type: 'success', text: 'Programare creată cu succes!' });
      setBookingMode(false);
      fetchPatientAppointments(selectedPatientId);
    } catch (error) {
      setMessage({ type: 'error', text: 'Eroare la crearea programării. Slotul s-ar putea să nu mai fie disponibil.' });
    }
  };

  const formatDateTime = (isoString) => {
    const d = new Date(isoString);
    return d.toLocaleString('ro-RO', { 
      day: '2-digit', month: '2-digit', year: 'numeric', 
      hour: '2-digit', minute: '2-digit' 
    });
  };

  return (
    <div className="w-full">
      <div className="bg-white p-8 rounded-2xl shadow-lg border border-gray-100">
        <h2 className="text-3xl font-bold text-gray-800 mb-6 flex items-center gap-3">
          <Calendar className="text-indigo-600" size={32} />
          Centru de Programări
        </h2>

        {message && (
          <div className={`mb-6 p-4 rounded-lg flex items-center gap-3 ${message.type === 'success' ? 'bg-green-50 text-green-700' : 'bg-red-50 text-red-700'}`}>
            {message.type === 'success' ? <CheckCircle2 /> : <AlertCircle />}
            {message.text}
          </div>
        )}

        <div className="mb-8 p-4 bg-indigo-50 rounded-xl">
          <label className="block text-sm font-bold text-indigo-900 mb-2">Selectează Pacientul</label>
          <select 
            className="w-full border-none rounded-lg p-3 shadow-sm bg-white focus:ring-2 focus:ring-indigo-500"
            value={selectedPatientId}
            onChange={(e) => setSelectedPatientId(e.target.value)}
          >
            <option value="">Alege un pacient din listă...</option>
            {patients.map(p => <option key={p.id} value={p.id}>{p.name} (ID: {p.id})</option>)}
          </select>
        </div>

        {selectedPatientId && (
          <div>
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-xl font-bold text-gray-700">Programările Pacientului</h3>
              {!bookingMode && (
                <button 
                  onClick={() => setBookingMode(true)}
                  className="bg-indigo-600 text-white px-6 py-2 rounded-full font-semibold hover:bg-indigo-700 transition-all shadow-md"
                >
                  Programează Vizită Nouă
                </button>
              )}
            </div>

            {bookingMode ? (
              <div className="bg-gray-50 p-6 rounded-xl border-2 border-dashed border-indigo-200 animate-in fade-in slide-in-from-top-4 duration-300">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <label className="block text-sm font-semibold text-gray-600 mb-2">Serviciu Medical</label>
                    <select 
                      className="w-full border p-3 rounded-lg bg-white"
                      value={bookingData.medicalServiceId}
                      onChange={(e) => setBookingData({...bookingData, medicalServiceId: e.target.value})}
                    >
                      <option value="">Alege serviciu...</option>
                      {services.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
                    </select>
                  </div>
                  <div>
                    <label className="block text-sm font-semibold text-gray-600 mb-2">Data</label>
                    <div className="flex gap-2">
                      <input 
                        type="date" 
                        className="flex-1 border p-3 rounded-lg"
                        value={bookingData.date}
                        min={new Date().toISOString().split('T')[0]}
                        onChange={(e) => setBookingData({...bookingData, date: e.target.value})}
                      />
                      <button 
                        onClick={handleFetchSlots}
                        className="bg-gray-800 text-white px-4 rounded-lg hover:bg-black"
                      >
                        Caută Sloturi
                      </button>
                    </div>
                  </div>
                </div>

                {loadingSlots && <p className="mt-4 text-center text-indigo-600 font-medium">Se caută sloturi libere...</p>}

                {availableSlots.length > 0 && (
                  <div className="mt-6">
                    <label className="block text-sm font-semibold text-gray-600 mb-3">Sloturi disponibile</label>
                    <div className="grid grid-cols-3 sm:grid-cols-4 md:grid-cols-6 gap-3">
                      {availableSlots.map((slot) => (
                        <button
                          key={slot}
                          onClick={() => setBookingData({...bookingData, timeSlot: slot})}
                          className={`p-3 rounded-lg text-sm font-medium transition-all ${
                            bookingData.timeSlot === slot 
                            ? 'bg-indigo-600 text-white ring-4 ring-indigo-200' 
                            : 'bg-white border hover:border-indigo-500 text-gray-700'
                          }`}
                        >
                          {new Date(slot).toLocaleTimeString('ro-RO', { hour: '2-digit', minute: '2-digit' })}
                        </button>
                      ))}
                    </div>
                  </div>
                )}

                <div className="mt-8 flex justify-end gap-3">
                  <button 
                    onClick={() => {setBookingMode(false); setAvailableSlots([]);}}
                    className="px-6 py-2 text-gray-600 font-semibold hover:bg-gray-200 rounded-full"
                  >
                    Renunță
                  </button>
                  <button 
                    onClick={handleBook}
                    disabled={!bookingData.timeSlot}
                    className="px-8 py-2 bg-indigo-600 text-white font-bold rounded-full shadow-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-indigo-700"
                  >
                    Confirmă Programarea
                  </button>
                </div>
              </div>
            ) : (
              <div className="space-y-3">
                {patientAppointments.map((appt) => (
                  <div key={appt.id} className="bg-white p-4 rounded-xl border flex items-center justify-between hover:shadow-sm">
                    <div className="flex items-center gap-4">
                      <div className="p-3 bg-indigo-50 text-indigo-600 rounded-full">
                        <Clock size={24} />
                      </div>
                      <div>
                        <p className="font-bold text-gray-800">{appt.medicalService?.name}</p>
                        <p className="text-sm text-gray-500">{formatDateTime(appt.appointmentFrom)}</p>
                      </div>
                    </div>
                    <div className="flex items-center gap-6">
                      <div className="text-right">
                        <span className={`px-3 py-1 rounded-full text-xs font-bold ${
                          appt.status === 'Completed' ? 'bg-gray-100 text-gray-600' : 'bg-blue-100 text-blue-700'
                        }`}>
                          {appt.status}
                        </span>
                        <p className="text-xs text-gray-400 mt-1">ID: #{appt.id}</p>
                      </div>
                    </div>
                  </div>
                ))}
                {patientAppointments.length === 0 && (
                  <div className="text-center py-12 border-2 border-dashed rounded-2xl text-gray-400">
                    Acest pacient nu are programări viitoare.
                  </div>
                )}
              </div>
            )}
          </div>
        )}

        {!selectedPatientId && (
          <div className="text-center py-20 text-gray-400">
            <Calendar size={64} className="mx-auto mb-4 opacity-20" />
            <p className="text-lg">Selectează un pacient pentru a-i vedea sau crea programări.</p>
          </div>
        )}
      </div>
    </div>
  );
}

