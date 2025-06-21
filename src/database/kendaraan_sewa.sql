-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Waktu pembuatan: 21 Jun 2025 pada 16.43
-- Versi server: 10.4.32-MariaDB
-- Versi PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `kendaraan_sewa`
--

-- --------------------------------------------------------

--
-- Struktur dari tabel `kasir`
--

CREATE TABLE `kasir` (
  `id_kasir` varchar(10) NOT NULL,
  `password` varchar(255) NOT NULL,
  `nama` varchar(255) NOT NULL,
  `no_telepon` varchar(25) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data untuk tabel `kasir`
--

INSERT INTO `kasir` (`id_kasir`, `password`, `nama`, `no_telepon`) VALUES
('K1', '123', 'Ahmad Yoga', '11111');

-- --------------------------------------------------------

--
-- Struktur dari tabel `kendaraan`
--

CREATE TABLE `kendaraan` (
  `id_kendaraan` varchar(10) NOT NULL,
  `jenis` varchar(10) NOT NULL,
  `merk` varchar(50) NOT NULL,
  `model` varchar(50) NOT NULL,
  `tahun` int(10) NOT NULL,
  `plat_nomor` varchar(15) NOT NULL,
  `harga_sewa` int(255) NOT NULL,
  `status` varchar(20) NOT NULL,
  `gambar` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data untuk tabel `kendaraan`
--

INSERT INTO `kendaraan` (`id_kendaraan`, `jenis`, `merk`, `model`, `tahun`, `plat_nomor`, `harga_sewa`, `status`, `gambar`) VALUES
('KD001', 'Mobil', 'Ferrari', 'Ferrari F8 Spider', 2025, 'B 1 KE', 2000000, 'Disewa', '477b42be-3458-4a7d-a482-107cec41e3cc.png'),
('KD002', 'Motor', 'Honda', 'Vario 150', 2019, 'B 4110 SSS', 200000, 'Tersedia', '5751bc0f-dbab-4379-80f3-1681fb1d681f.jpg'),
('KD003', 'Motor', 'Honda', 'Honda Scoopy Sporty', 2021, 'B 1234 SZE', 200000, 'Tersedia', '3f8f4881-2226-4fe0-a8d8-5b28e1190df6.jpg'),
('KD004', 'Motor', 'Honda', 'Honda Genio CBS', 2021, 'B 1212 TZY', 150000, 'Tersedia', '54a3cbbb-8509-48c7-9907-8f5c60d48f7f.jpg');

-- --------------------------------------------------------

--
-- Struktur dari tabel `pelanggan`
--

CREATE TABLE `pelanggan` (
  `id_pelanggan` varchar(10) NOT NULL,
  `nama_pelanggan` varchar(255) NOT NULL,
  `alamat` text NOT NULL,
  `no_telepon` varchar(20) NOT NULL,
  `ktp` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data untuk tabel `pelanggan`
--

INSERT INTO `pelanggan` (`id_pelanggan`, `nama_pelanggan`, `alamat`, `no_telepon`, `ktp`) VALUES
('PL001', 'Marsel', 'Depok', '123412341234', '7a4b118c-48cc-43bf-869b-5b764b28afb2.png'),
('PL002', 'Revo', 'Jakarta', '1231234123', 'd572a085-36a5-4436-a893-b1fa8933d8d9.png'),
('PL003', 'Yoga', 'Jakarta', '123', 'febc5dbc-edbb-4f99-bd11-88e3335d00ce.png');

-- --------------------------------------------------------

--
-- Struktur dari tabel `transaksi`
--

CREATE TABLE `transaksi` (
  `id_transaksi` varchar(10) NOT NULL,
  `id_kasir` varchar(10) NOT NULL,
  `id_pelanggan` varchar(10) NOT NULL,
  `id_kendaraan` varchar(10) NOT NULL,
  `tanggal_transaksi` date NOT NULL,
  `tanggal_sewa` date NOT NULL,
  `tanggal_kembali` date NOT NULL,
  `durasi_sewa` int(100) NOT NULL,
  `total_biaya` int(100) NOT NULL,
  `status_transaksi` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data untuk tabel `transaksi`
--

INSERT INTO `transaksi` (`id_transaksi`, `id_kasir`, `id_pelanggan`, `id_kendaraan`, `tanggal_transaksi`, `tanggal_sewa`, `tanggal_kembali`, `durasi_sewa`, `total_biaya`, `status_transaksi`) VALUES
('TR001', 'K1', 'PL001', 'KD001', '2025-06-21', '2025-06-21', '2025-06-29', 9, 18000000, 'Berjalan');

--
-- Indexes for dumped tables
--

--
-- Indeks untuk tabel `kasir`
--
ALTER TABLE `kasir`
  ADD PRIMARY KEY (`id_kasir`);

--
-- Indeks untuk tabel `kendaraan`
--
ALTER TABLE `kendaraan`
  ADD PRIMARY KEY (`id_kendaraan`);

--
-- Indeks untuk tabel `pelanggan`
--
ALTER TABLE `pelanggan`
  ADD PRIMARY KEY (`id_pelanggan`);

--
-- Indeks untuk tabel `transaksi`
--
ALTER TABLE `transaksi`
  ADD PRIMARY KEY (`id_transaksi`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
